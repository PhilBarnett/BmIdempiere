# Place Orders into Production

The MTM Manufacturing plug-in exposes a server-side iDempiere process for an explicit list of sales order numbers or a selected Installation. Inspection is the default. Release completes reviewed orders and creates or resumes their MTM runs through the ERP document actions. It saves each run's factory paperwork to the configured destination and offers a single ZIP from an interactive ERP session. A headless caller can retrieve the files from the process log paths using the factory's authorized file access. Installation status stays under Phil's control.

## Set up on staging

1. Build and install an updated `au.blindmot.mtomman` bundle using the existing iDempiere 12 Eclipse PDE target. Restart or refresh the bundle as required by the installation.
2. In **Report & Process**, add an active Java process with search key `BLD_ReleaseMtmOrders`, name **Place Orders into Production**, class `au.blindmot.processes.mtmcreate.ReleaseMtmOrders`, data access level **Client + Organization**, and **Report** cleared. Add a process entry in **Menu** and grant access to the factory role. The role needs access to the selected orders, Installation, MTM records, both document types' **Complete** action, and the five source report processes.
3. Add the following parameters. Leave both selectors optional in the dictionary. The process requires exactly one selector at runtime.

   | Seq | Name | DB column name | Reference | Default |
   |---|---|---|---|---|
   | 10 | Sales order numbers | `OrderNumbers` | String (length 2000) | |
   | 20 | Installation | `Bld_Mtm_Install_ID` | Search, using the Installation table/key | |
   | 30 | Release reviewed orders | `ReleaseReviewedOrders` | Yes-No | `N` |

4. Set `BLD_MTM_PAPERWORK_DIRECTORY` for the client or organisation to an existing, absolute, ERP-writable path on the factory paperwork volume. Use a directory restricted to authorized factory staff and the ERP service account. Each saved run goes in a client/organisation/run subdirectory. The browser ZIP omits checksum files.
5. Confirm the active **Print Mtm Labels**, **MTM Aluminium cuts**, **MTM Fabric cuts**, **MTM Assembly**, and **MTM Info Summary** definitions in the target ERP. The four PDFs support native print formats with a `Bld_Mtom_Production_ID` column and Jasper reports scoped by `RECORD_ID` or their declared `Bld_Mtom_Production_ID` process parameter. A Jasper definition must already restrict its data to the supplied run. The label renderer uses the standard Java defaults of the existing label process. Compare report contents and label counts with the current workflow on staging.

## Run the process

1. In the correct ERP environment and organisation, choose the Installation or enter the exact sales order numbers, separated by commas or line breaks. Use **Accepted** in the Installation window when discovering jobs to release.
2. Run inspection with **Release reviewed orders** cleared. Check the process log for the job, attachment names, unchecked lines, and any existing production run. Review the approved measurement attachment on Installation first, then the order. Verify all order lines against the source and the saved ERP configuration, including WooCommerce specifications and any substitutions. Apply clear corrections in the normal ERP editor, default blank **Fixing** to **Timber**, and save **ischeckmeasured** on each verified MTM line. Leave ambiguous lines unchecked. Inspection lists attachment candidates. The operator selects the approved revision and confirms the review before release.
3. Run again with **Release reviewed orders** ticked. The process rejects unchecked MTM lines and ambiguous run coverage. Existing multi-order runs require all their orders in the same selection. New runs follow the existing per-order MTM creation logic. Each independent job can continue if another fails.
4. Open the process log and retrieve the ZIP or the verified files from the listed server paths. Confirm the actual download at the workstation. Files have names such as `SO-52145-MTM-1003447-aluminium-cuts.pdf`. An individual output failure is named in the log. Run the same selection again to recover missing or invalid files. A completed Installation is ready for Phil's manual **Pre Production** step once all linked orders' required paperwork is verified.

## Check before enabling on production

Use an explicitly nominated staging job with a checkmeasure attachment and at least one MTM line. Inspect it while unchecked, confirm release is blocked, then finish the normal line review. Release it, compare all five files with the ERP previews, and check the labels and aluminium PDF for the run number. Check that a second release produces the same run and reuses the verified files. On the staging volume, move one PDF aside and rerun to confirm only that PDF is regenerated. Check a wholesale order and an Installation with multiple linked orders, including a partial selection. Confirm the Installation status matches the starting status and that the ERP does not create a print-queue entry.
