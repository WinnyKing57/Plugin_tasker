import base64
import os
import sys

def create_minimal_png(path, filename):
    # Smallest valid PNG (1x1 transparent pixel)
    png_data = base64.b64decode(
        b'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII='
    )
    # Ensure the directory exists
    os.makedirs(path, exist_ok=True)
    filepath = os.path.join(path, filename)
    with open(filepath, 'wb') as f:
        f.write(png_data)
    print(f"Created {filepath}")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python create_png.py <path> <filename>")
        sys.exit(1)
    path_arg = sys.argv[1]
    filename_arg = sys.argv[2]
    create_minimal_png(path_arg, filename_arg)
