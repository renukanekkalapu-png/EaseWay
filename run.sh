#!/usr/bin/env bash
set -e

echo "========================================================================"
echo "       Compiling EaseWay — RVCE Mysuru Road Campus Application          "
echo "========================================================================"

mkdir -p bin
javac -d bin src/com/easeway/*.java test/com/easeway/*.java

echo "[SUCCESS] Compilation successful. Launching EaseWay..."
echo "========================================================================"
java -cp bin com.easeway.Main
