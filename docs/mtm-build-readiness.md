# MTM build readiness

The current project is an Eclipse PDE plugin using Java 17 and the
`org.eclipse.pde.core.requiredPlugins` container. No target definition is committed.
The local library entries under target/lib are not present in Git (34 missing
entries on the foundation branch). The Maven descriptor uses `pom` packaging,
version 8.2.1 and sourceDirectory `src`; it is not a complete build for the
manifest's 12.0.4 OSGi plugin. The repository's iDempiere 7.1 JARs in another
project are not an appropriate replacement for the production 12 target.

## Automated checks

The MTM foundation GitHub workflow runs the standalone Java 17 barcode checks
and Python dependency-inventory tests. It does not deploy anything, use ERP
credentials or certify an OSGi build. A green workflow is not rollout approval.

## Read-only inventory

From the repository root:

```sh
python3 scripts/check-mtm-build.py
python3 scripts/check-mtm-build.py --plugins /path/to/idempiere12/plugins
```

Exit 2 means missing or conflicting dependencies. The script only reads the
project metadata and JAR manifests in the supplied directory. It does not scan
other directories, extract files, modify the target, access the database, start
the ERP or deploy a bundle. Its output includes filenames and versions only.
Exit 0 means the inventory checks passed, not that compilation or OSGi resolution
passed. Exploded bundles and transitive dependency resolution are not supported
by this inventory; use the verified Eclipse target for those checks.

## Inputs needed for the full build

Prefer the existing Eclipse iDempiere 12 workspace/target used to create the
installed 12.0.4.202601021647 plugin. Establish the target provenance, source
commit and exact dependency versions before producing a replacement bundle.
Restore the plugin's third-party dependency libraries through the original build
process, verify the PDE source and classpath, and compile with Java 17. An
authorised read-only copy of installed plugin libraries may help investigate
differences, but is not automatically a reproducible development target.

Do not copy workspace settings, application configuration, tokens, database
credentials or customer data into Git. Only build metadata and required library
artifacts are relevant.

Then test model-factory construction against iDempiere 12, open all three barcode
targets in a non-production ERP, verify role/client/org restrictions, and verify
that lookup has no scan or stock side effects. Only after that should a production
bundle update be considered. No target or full plugin compile has been verified
in this branch yet.
