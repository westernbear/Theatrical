## research first

Before any substantive analysis, diagnosis, design, or implementation:

- Research the task first. Check current web sources, primary documentation/literature, and relevant papers before changing files.
- Prefer primary sources such as official documentation, source repositories, issue trackers, specifications, and original papers.
- Briefly report the findings and sources that will guide the work. Do not skip research because the task looks familiar.
- If a source category has no meaningful material for the task, state that result and continue; do not force an irrelevant citation.
- For codebase work, do this research first, then follow the graphify workflow below before raw source browsing.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
