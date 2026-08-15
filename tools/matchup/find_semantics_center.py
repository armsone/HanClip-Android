#!/usr/bin/env python3
"""Print the center of the first UIAutomator node with an exact description."""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET


def main() -> int:
    if len(sys.argv) == 3:
        attribute = "content-desc"
        expected = sys.argv[2]
    elif len(sys.argv) == 4 and sys.argv[2] in {"content-desc", "text"}:
        attribute = sys.argv[2]
        expected = sys.argv[3]
    else:
        print(
            "usage: find_semantics_center.py WINDOW_XML [content-desc|text] VALUE",
            file=sys.stderr,
        )
        return 2

    root = ET.parse(sys.argv[1]).getroot()
    for node in root.iter("node"):
        if node.attrib.get(attribute) != expected:
            continue
        match = re.fullmatch(r"\[(\d+),(\d+)]\[(\d+),(\d+)]", node.attrib["bounds"])
        if match is None:
            break
        left, top, right, bottom = map(int, match.groups())
        print((left + right) // 2, (top + bottom) // 2)
        return 0

    print(f"semantics node not found: {attribute}={expected}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
