![banner shape 2](https://i.ibb.co/yFGc2V40/banner-shape-2.png)

LioLoader adds **global datapacks** and **global resource packs** to your instance automatically by scanning special folders inside your game directory.

This is useful for keeping packs **outside of individual worlds** and applying them consistently across worlds and servers you run locally.

---

## Folder Layout

LioLoader uses a `lioloader` folder inside your instance:

- **Datapacks**
  - `instance/lioloader/data/`
- **Resource packs**
  - `instance/lioloader/resourcepacks/`

Anything placed in these folders will be discovered and added to Minecraft’s pack repositories.

Note: This mod is only needed server-side and needs Architectury API
---

## What Counts as a Pack?

### Supported formats (both datapacks and resource packs)

LioLoader supports:

1. **Zip packs**
   - Any `*.zip` inside the folder is treated as a pack.

2. **Normal pack folders**
   - A folder is treated as a pack if it contains:
     - `pack.mcmeta`
     - and the proper content folder:
       - Datapacks: `data/`
       - Resource packs: `assets/`

3. **Datapacks inside nested folders**

   Datapacks can be stored inside other folders, as long as the folder you want treated as the pack root contains:

   - `pack.mcmeta`
   - `data/`

   Example (nested datapack structure):

   - `instance/lioloader/data/somefolder/pack.mcmeta`
   - `instance/lioloader/data/somefolder/data/...`

   This lets you organize datapacks however you want while still being detected.

---

## Load Order / Priority

LioLoader supports **explicit pack ordering** using JSON files generated in your `lioloader` folder.

These files are automatically created if missing:

- `instance/lioloader/datapack_load_order.json`
- `instance/lioloader/resourcepack_load_order.json`

### Format

Each file contains an `order` array:

```json
{
  "order": [
    "highest_priority_example",
    "second_priority_example",
    "lowest_priority_example"
  ]
}
````

### How ordering works

* Packs listed first are treated as **higher priority**.
* Packs not listed still load normally (Minecraft defaults).
* Packs with lower priorities will overwrite packs with higher priorities that get read/loaded in first.

### Pack ID notes

* For zip packs, you can write the name with or without `.zip`:

    * `my_pack.zip` or `my_pack`
* For folder packs, use the folder name:

    * `my_pack_folder`

---

## Quick Example

Put a datapack here:

* `instance/lioloader/data/MyGlobalPack/pack.mcmeta`
* `instance/lioloader/data/MyGlobalPack/data/<namespace>/...`

Put a resource pack here:

* `instance/lioloader/resourcepacks/MyGlobalRP/pack.mcmeta`
* `instance/lioloader/resourcepacks/MyGlobalRP/assets/<namespace>/...`

Then set priority:

* `instance/lioloader/datapack_load_order.json`
* `instance/lioloader/resourcepack_load_order.json`

---

## Community

[![Discord](https://i.ibb.co/qDNhg49/636e0a6a49cf127bf92de1e2-icon-clyde-blurple-RGB.png)](https://discord.gg/sPHes7q4Pr)

![usageinmodpacks](https://i.ibb.co/hJvQHRN5/usageinmodpacks.png)

**Feel free to include LioLoader in any pack of your choosing!**

