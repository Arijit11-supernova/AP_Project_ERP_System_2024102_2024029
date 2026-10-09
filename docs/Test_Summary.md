# University ERP – Test Summary

All test cases in `Test_Plan.md` were executed through the GUI.

| Metric | Count |
|--------|-------|
| Designed | 15 |
| Executed | 15 |
| Passed | 15 |
| Failed | 0 |

| Module | Result |
|--------|--------|
| Login & roles | Passed |
| Student module | Passed |
| Instructor module | Passed |
| Admin module | Passed |
| Maintenance mode | Passed |
| Security (lockout, unauthorized access) | Passed |

**Edge cases covered:** duplicate registration (TC5), full section (TC14), unauthorized access (TC15), maintenance-mode restrictions (TC12).

**Known issues / limitations:** no major bugs found. Edge-case validation is basic but sufficient for project scope; the UI is functional rather than heavily optimized.

**Conclusion:** the system is stable and ready for demonstration and evaluation.
