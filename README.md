# Huffman Encoding System

A lossless file compression and decompression tool built in **Java** with a **Swing** GUI. Uses Huffman encoding to reduce file sizes by up to **80%** through variable-length binary encodings based on byte frequency.

---

## Features

- **Lossless compression** — original files are reconstructed exactly after decompression
- **Bit-level I/O** — processes files byte-by-byte, converting data to compact binary encodings
- **Two header formats** with distinct trade-offs:
  - **Standard Count Format** — fixed-size header, consistent regardless of file content
  - **Standard Tree Format** — variable-size header, more space-efficient for files with fewer unique byte values
- **Single file mode** — compress or decompress one file at a time via the GUI (`Huff.java`)
- **Batch mode** — compress an entire folder at once (`HuffMark.java`)

---

## How It Works

1. **Frequency analysis** — scans the input file and counts how often each byte value appears
2. **Tree construction** — builds a Huffman tree using a priority queue (`NodeQueue.java`, `TreeNode.java`), where less frequent bytes get longer codes and more frequent bytes get shorter ones
3. **Encoding** — writes a header (either Count or Tree format) followed by the bit-encoded file content using `BitOutputStream`
4. **Decoding** — reads the header to reconstruct the Huffman tree, then traverses it bit-by-bit to recover the original data using `BitInputStream`

---

## Project Structure

| File | Description |
|---|---|
| `SimpleHuffProcessor.java` | Core compression and decompression logic |
| `HuffTree.java` | Builds and traverses the Huffman tree |
| `TreeNode.java` | Node structure for the Huffman tree |
| `NodeQueue.java` | Priority queue used during tree construction |
| `BitInputStream.java` | Reads data at the bit level |
| `BitOutputStream.java` | Writes data at the bit level |
| `GUIHuffViewer.java` | Swing-based graphical interface |
| `TextHuffViewer.java` | Text-based viewer alternative |
| `Huff.java` | Entry point for single-file compression/decompression |
| `HuffMark.java` | Entry point for batch folder compression |
| `IHuffProcessor.java` | Interface defining processor contract |
| `IHuffViewer.java` | Interface defining viewer contract |
| `IHuffConstants.java` | Shared constants (magic numbers, header format flags, etc.) |
| `Diff.java` | Utility to verify that decompressed output matches original |

---

## Getting Started

### Prerequisites

- Java 8 or higher

### Running

**Single file** — run `Huff.java` and use the GUI to select a file to compress or decompress.

**Batch mode** — run `HuffMark.java` and use the GUI to select a folder; all files in the folder will be compressed.

---

## Header Formats

| Format | Header Size | Best For |
|---|---|---|
| Standard Count Format | Fixed (256 × 4 bytes) | Files with many unique byte values |
| Standard Tree Format | Variable | Files with fewer unique byte values |

Both formats embed all information needed to reconstruct the Huffman tree at decode time — no external dictionary required.

---

## Tech Stack

- **Language:** Java
- **GUI:** Java Swing
- **Algorithm:** Huffman Coding (greedy, frequency-based)
