#!/bin/sh
# Runs an x86-64 Linux build tool on this ARM64 build host. This is NOT an
# Android app runtime; guest programs need their own libraries and isolation.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
QEMU="$ROOT/extracted/usr/bin/qemu-x86_64-static"
LIB="$ROOT/x86-64/usr/lib/x86_64-linux-gnu"
LOADER="$LIB/ld-linux-x86-64.so.2"
if [ "$#" -lt 1 ]; then
    printf 'usage: %s /absolute/path/to/x86-64-program [arguments...]\n' "$0" >&2
    exit 2
fi
if [ ! -x "$QEMU" ] || [ ! -f "$LOADER" ]; then
    printf 'QEMU or x86-64 glibc is missing from %s\n' "$ROOT" >&2
    exit 1
fi
exec "$QEMU" "$LOADER" --library-path "$LIB${SDK_LIB_PATH:+:$SDK_LIB_PATH}" "$@"
