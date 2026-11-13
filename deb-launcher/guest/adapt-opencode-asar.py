#!/usr/bin/env python3
"""Adapt the specific OpenCode Desktop 2.2.6 ASAR for ARM64 Electron.

This copies into a new directory, validates archive members, checks hashes and
changes only the node-pty import. The caller supplies the verified ARM64
node-pty package. Never modifies the input ASAR or its unpacked directory.
"""
import hashlib
import json
import os
from pathlib import Path
import shutil
import struct
import sys
import tarfile

ASAR_SHA256 = '21d1a1ccf4768421282b57e521c282644822a88b589eab4565fd034291695de8'
PTY_SHA256 = '7fe377bd4944f3722f50db97a0a439defc2b1faeea526659323b3dfc9e7c5a84'
OLD = b'@lydell/node-pty-linux-x64'
NEW = b'@lydell/node-pty-linux-arm64'


def sha256(path):
    h = hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def checked_parts(parts):
    if not parts or any(not p or p in ('.', '..') or '/' in p or '\\' in p for p in parts):
        raise ValueError('unsafe archive path')
    return parts


def members(node, parts=()):
    files = node.get('files')
    if not isinstance(files, dict):
        raise ValueError('invalid ASAR directory')
    for name, entry in files.items():
        if not isinstance(name, str) or not isinstance(entry, dict):
            raise ValueError('invalid ASAR entry')
        path = checked_parts(parts + (name,))
        if 'files' in entry:
            yield from members(entry, path)
        elif 'link' in entry:
            raise ValueError('ASAR symlinks are not supported')
        else:
            yield path, entry


def copy_exact(source, dest, size, expected):
    h = hashlib.sha256()
    remaining = size
    with dest.open('xb') as out:
        while remaining:
            chunk = source.read(min(1024 * 1024, remaining))
            if not chunk:
                raise ValueError('truncated ASAR entry')
            out.write(chunk)
            h.update(chunk)
            remaining -= len(chunk)
    if expected and h.hexdigest() != expected:
        raise ValueError('ASAR entry integrity mismatch: ' + str(dest))


def extract_asar(archive, unpacked, out):
    total_size = archive.stat().st_size
    with archive.open('rb') as f:
        raw = f.read(16)
        if len(raw) != 16:
            raise ValueError('truncated ASAR header')
        first, header_size, pickle_size, json_size = struct.unpack('<4I', raw)
        if first != 4 or header_size < 8 or json_size > header_size - 8 or header_size > 64 * 1024 * 1024:
            raise ValueError('invalid ASAR header')
        tree = json.loads(f.read(json_size))
        start = 8 + header_size
        entries = list(members(tree))
        if len(entries) > 50000:
            raise ValueError('too many ASAR entries')
        total = 0
        unpacked_root = unpacked.resolve()
        for parts, entry in entries:
            size = entry.get('size')
            if not isinstance(size, int) or isinstance(size, bool) or size < 0 or size > 512 * 1024 * 1024:
                raise ValueError('invalid ASAR entry size')
            total += size
            if total > 2 * 1024 * 1024 * 1024:
                raise ValueError('ASAR exceeds extraction limit')
            dest = out.joinpath(*parts)
            dest.parent.mkdir(parents=True, exist_ok=True)
            integrity = entry.get('integrity') or {}
            if integrity and integrity.get('algorithm') != 'SHA256':
                raise ValueError('unsupported ASAR integrity algorithm')
            expected = integrity.get('hash')
            if entry.get('unpacked'):
                src = unpacked.joinpath(*parts)
                if not src.resolve().is_relative_to(unpacked_root) or src.is_symlink() or not src.is_file():
                    raise ValueError('invalid unpacked ASAR entry')
                with src.open('rb') as inp:
                    copy_exact(inp, dest, size, expected)
            else:
                offset = int(entry.get('offset', -1))
                if offset < 0 or start + offset + size > total_size:
                    raise ValueError('ASAR entry outside archive')
                f.seek(start + offset)
                copy_exact(f, dest, size, expected)
            os.chmod(dest, 0o755 if entry.get('executable') else 0o644)
    return len(entries)


def install_pty(archive, out):
    dest = out / 'node_modules/@lydell/node-pty-linux-arm64'
    if dest.exists():
        raise ValueError('ARM64 node-pty already exists')
    dest.mkdir(parents=True)
    with tarfile.open(archive, 'r:gz') as t:
        for m in t:
            parts = tuple(Path(m.name).parts)
            if len(parts) < 2 or parts[0] != 'package':
                raise ValueError('unexpected node-pty archive path')
            checked_parts(parts)
            target = dest.joinpath(*parts[1:])
            if m.isdir():
                target.mkdir(parents=True, exist_ok=True)
            elif m.isfile() and m.size <= 5 * 1024 * 1024:
                target.parent.mkdir(parents=True, exist_ok=True)
                with t.extractfile(m) as inp, target.open('xb') as output:
                    shutil.copyfileobj(inp, output)
                os.chmod(target, 0o755 if m.mode & 0o111 else 0o644)
            else:
                raise ValueError('unsupported node-pty archive entry')


def main():
    if len(sys.argv) != 5:
        raise SystemExit('usage: adapt-opencode-asar.py ASAR UNPACKED PTY_TGZ OUTPUT_DIR')
    archive, unpacked, pty, out = map(Path, sys.argv[1:])
    if sha256(archive) != ASAR_SHA256 or sha256(pty) != PTY_SHA256:
        raise SystemExit('Unrecognized OpenCode ASAR or ARM64 node-pty archive')
    if out.exists():
        raise SystemExit('Output already exists; refusing to overwrite it')
    out.mkdir(parents=True)
    count = extract_asar(archive, unpacked, out)
    matches = []
    for file in (out / 'out/main').glob('*.js'):
        data = file.read_bytes()
        if OLD in data:
            matches.append((file, data))
    if len(matches) != 1 or matches[0][1].count(OLD) != 1:
        raise ValueError('Unexpected OpenCode node-pty import layout')
    file, data = matches[0]
    file.write_bytes(data.replace(OLD, NEW))
    install_pty(pty, out)
    print(f'Adapted {count} ASAR files for ARM64 into {out}')


if __name__ == '__main__':
    main()
