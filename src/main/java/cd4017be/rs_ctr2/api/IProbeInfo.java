package cd4017be.rs_ctr2.api;

/**Implemented by GridParts or TileEntities to display state information on a Gate Probe.
 * @author CD4017BE */
public interface IProbeInfo {

	/**@return ["format_lang_key", args...]
	 * to display on Gate Probe (called server side) */
	Object[] stateInfo();

}
