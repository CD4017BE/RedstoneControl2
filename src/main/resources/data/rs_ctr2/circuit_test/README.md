##Advancement Trigger

Redstone Control 2 adds the `rs_ctr2:circuit_test` advancement trigger that gets activated when a player clicks the **Unlock part** button in the **Redstone Research Lab** after having successfully completed a circuit test. The trigger can be used in the `criteria` block of an advancement JSON file as follows:

```JSON
...
"criteria": {
	"<criteria_name>" {
		"trigger": "rs_ctr2:circuit_test",
		"conditions": {
			"test": "<namespace>:<testID>"
		}
	}
}
```
Here, `<namespace>:<testID>` specifies the test that must be completed, which is defined through the file `data/<namespace>/circuit_test/<testID>.csv`.

##Test Format

Tests to be listed in the **Redstone Research Lab** must be defined as `.csv` (comma separated values) files in the `data/<namespace>/circuit_test/` directory of a mod or data pack. You can edit these files with a normal text editor or with spreadsheet editing programs like Excel or OpenOffice/LibreOffice Calc.

The file should contain a table, where rows are separated by new lines (`\n`, `\r` or `\r\n`) and columns are separated by commas  (`,`), excess whitespace around entries is ignored.
The first row is a header that defines which input / output ports of the **Redstone Research Lab** each column corresponds to (`A`, `B`, `C`, `D` for supplied circuit inputs and `X`, `Y` for expected circuit outputs).
All rows following the header contain the signal values for the test, given as either signed decimal integers or as hexadecimal integers prefixed with `0x`.

Tests may also have configuration lines starting with `#` **before the header row**:
- `#name=<langKey>`: specifies a translation key for the displayed name of the test. By default `#name=circuit_test.<namespace>.<testID>`.
- `#level=<n>`: controls where the test shows up in the test list relative to others. By default `#level=0`, higher values sink towards the bottom of the list.
- `#shuffle`: makes the test go through its rows in a randomized order rather than sequential for each run.
- other lines throughout the file starting with `#` are ignored.
