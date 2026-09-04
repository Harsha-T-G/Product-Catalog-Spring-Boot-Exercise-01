#!/usr/bin/env bash
# Optional: install addyosmani SDD + TDD skills globally for other projects.
# This repository vendors complete skills under .agents/skills/ — agents should
# use those files directly without running this script.
#
# Run from repo root: ./scripts/setup-agent-skills.sh [--global]

set -euo pipefail

REPO="https://github.com/addyosmani/agent-skills"
SKILLS="spec-driven-development,test-driven-development"
GLOBAL="${1:-}"

echo "NOTE: Product Catalog agents should use vendored skills in .agents/skills/"
echo "This script only installs optional global copies from ${REPO}"

if [[ "${GLOBAL}" == "--global" || "${GLOBAL}" == "-g" ]]; then
  npx skills add "${REPO}" -g -y -s spec-driven-development -s test-driven-development -a cursor
  echo "Installed globally under ~/.agents/skills/"
else
  npx skills add "${REPO}" -y -s spec-driven-development -s test-driven-development -a cursor
  echo "Installed for this project. Run: npx skills list"
fi
