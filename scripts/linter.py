import sys
from pylint import lint

# Wrapper for pylint ----------------------
#   exit with 1 if linter score < THRESHOLD
#  ------------------------------------------

THRESHOLD = 8

if len(sys.argv) < 2:
    print("Usage: python linter.py <file/directory> <pylint options>")
    sys.exit(1)
run = lint.Run(sys.argv[1:], do_exit=False)
score = run.linter.stats["global_note"]

if score < THRESHOLD:
    sys.exit(1)