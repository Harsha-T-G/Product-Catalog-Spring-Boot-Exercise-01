#!/usr/bin/env bash
# Install addyosmani SDD + TDD skills for agents working on this repository.
# Run from repo root: ./scripts/setup-agent-skills.sh
# Add -g to install globally instead of project-scoped.

set -euo pipefail

REPO="https://github.com/addyosmani/agent-skills"
SKILLS="spec-driven-development,test-driven-development"
GLOBAL="${1:-}"

echo "Installing skills from ${REPO}: ${SKILLS}"

if [[ "${GLOBAL}" == "--global" || "${GLOBAL}" == "-g" ]]; then
  npx skills add "${REPO}" -g -y -s spec-driven-development -s test-driven-development -a cursor
  echo "Installed globally under ~/.agents/skills/"
else
  npx skills add "${REPO}" -y -s spec-driven-development -s test-driven-development -a cursor
  echo "Installed for this project. Run: npx skills list"
fi

echo "Bootstrap stubs remain at .agents/skills/*/SKILL.md for clone-friendly routing."
