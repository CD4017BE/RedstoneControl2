This is the Minecraft 1.16 port of my previous mod [Redstone Control](https://github.com/CD4017BE/RedstoneControl), but everything is microblocks now.

**RedstoneControl2** uses my new microblock system in [CD4017BE_lib](https://github.com/CD4017BE/CD4017BE_lib) to add miniature logic gates, arithmetic operators and control interface elements (switches, lamps, 7-segment display) for Redstone Signals to Minecraft. To give you something to control with all the fancy circuitry, it also comes with its own microblock based Item, Fluid and Energy transport systems and machines. And a remote Block Interaction system allows devices like `Block Breakers` to operate not just on the block in front of them, but on blocks selected by `Interaction Pipe Drivers` (extends a controllable pipe) or `Random Block Access Controllers` (select blocks inside a marked cubiod region). Offering countless automation possibilities and alongside circuit engineering challenges.

![](cover_image.png)

To not put unnecessary burden on your CPU, the microblocks are hosted by non-ticking TileEntities and have **zero** idle¹ performance impact (they only cost RAM). Microblock devices also communicate directly without block updates, so even complicated active microblock circuits often have a much lower server performance impact than just a single piece of redstone dust being pulsed.  
*¹: idle includes decorative structures, logic gates in a stable state and inactive machines.*

## Project Setup
The setup is the same as for [CD4017BE_lib](https://github.com/CD4017BE/CD4017BE_lib#project-setup-for-mc-1165).
Gradle will require GitHub authentication for downloading the CD4017BE_lib dependencies [as described here](https://github.com/CD4017BE/CD4017BE_lib#using-this-mod-as-dependency-1165).
