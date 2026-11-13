#!/bin/sh
# Build only the matching Tessera PRoot loader with its ELF headers away from
# 0x200000. The native x86-64 OpenCode CLI requests that address. This does not
# patch or replace the running environment's PRoot.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
P="$ROOT/proot-source/src"
OUT="$ROOT/proot-loader-output"
CC="$ROOT/ndk-qemu/clang"
mkdir -p "$OUT"
test -f "$P/loader/loader.c"
FLAGS="-D_FILE_OFFSET_BITS=64 -D_GNU_SOURCE -I$P -O2 -fPIC -ffreestanding"
# LOADER_ADDRESS for the upstream aarch64 build is 0x2000000000; place the
# read-only ELF header at a distinct high address below it as well.
"$CC" --target=aarch64-linux-android24 $FLAGS -c "$P/loader/loader.c" -o "$OUT/loader.o"
"$CC" --target=aarch64-linux-android24 $FLAGS -E "$P/loader/assembly.S" -o "$OUT/assembly.s"
"$CC" --target=aarch64-linux-android24 $FLAGS -c "$OUT/assembly.s" -o "$OUT/assembly.o"
"$CC" --target=aarch64-linux-android24 -static -nostdlib \
    -Wl,--image-base=0x1fffff0000,--build-id=none,-Ttext=0x2000000000,--rosegment,-z,noexecstack \
    -o "$OUT/libproot-loader.so" "$OUT/loader.o" "$OUT/assembly.o"
readelf -lW "$OUT/libproot-loader.so" | grep LOAD
printf 'Built: %s\n' "$OUT/libproot-loader.so"
