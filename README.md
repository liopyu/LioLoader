<p><img src="https://i.ibb.co/yFGc2V40/banner-shape-2.png" alt="banner shape 2"></p>
<p>LioLoader adds <strong>global datapacks</strong> and <strong>global resource packs</strong> to your instance automatically by scanning special folders inside your game directory.</p>
<p>This is useful for keeping packs <strong>outside of individual worlds</strong> and applying them consistently across worlds and servers you run locally.</p>
<hr>
<h2>Folder Layout</h2>
<p>LioLoader uses a <code>lioloader</code> folder inside your instance:</p>
<ul>
<li><strong>Datapacks</strong>
<ul>
<li><code>instance/lioloader/data/</code></li>
</ul>
</li>
<li><strong>Resource packs</strong>
<ul>
<li><code>instance/lioloader/resourcepacks/</code></li>
</ul>
</li>
</ul>
<p>Anything placed in these folders will be discovered and added to Minecraft&rsquo;s pack repositories.</p>
<hr>
<h2>What Counts as a Pack?</h2>
<h3>Supported formats (both datapacks and resource packs)</h3>
<p>LioLoader supports:</p>
<ol>
<li><strong>Zip packs</strong>
<ul>
<li>Any <code>*.zip</code> inside the folder is treated as a pack.</li>
</ul>
</li>
<li><strong>Normal pack folders</strong>
<ul>
<li>A folder is treated as a pack if it contains:
<ul>
<li><code>pack.mcmeta</code></li>
<li>and the proper content folder:
<ul>
<li>Datapacks: <code>data/</code></li>
<li>Resource packs: <code>assets/</code></li>
</ul>
</li>
</ul>
</li>
</ul>
</li>
<li><strong>Datapacks inside nested folders</strong>
<p>Datapacks can be stored inside other folders, as long as the folder you want treated as the pack root contains:</p>
<ul>
<li><code>pack.mcmeta</code></li>
<li><code>data/</code></li>
</ul>
<p>Example (nested datapack structure):</p>
<ul>
<li><code>instance/lioloader/data/somefolder/pack.mcmeta</code></li>
<li><code>instance/lioloader/data/somefolder/data/...</code></li>
</ul>
<p>This lets you organize datapacks however you want while still being detected.</p>
</li>
</ol>
<hr>
<h2>Load Order / Priority</h2>
<p>LioLoader supports <strong>explicit pack ordering</strong> using JSON files generated in your <code>lioloader</code> folder.</p>
<p>These files are automatically created if missing:</p>
<ul>
<li><code>instance/lioloader/datapack_load_order.json</code></li>
<li><code>instance/lioloader/resourcepack_load_order.json</code></li>
</ul>
<h3>Format</h3>
<p>Each file contains an <code>order</code> array:</p>
<pre><code>{
  "order": [
    "highest_priority_example",
    "second_priority_example",
    "lowest_priority_example"
  ]
}</code></pre>
<h3>How ordering works</h3>
<ul>
<li>Packs listed first are treated as <strong>higher priority</strong>.</li>
<li>Packs not listed still load normally (Minecraft defaults).</li>
<li>Packs with lower priority overwrite packs with higher priority that get read first.</li>
</ul>
<h3>Pack ID notes</h3>
<ul>
<li>For zip packs, you can write the name with or without <code>.zip</code>:
<ul>
<li><code>my_pack.zip</code> or <code>my_pack</code></li>
</ul>
</li>
<li>For folder packs, use the folder name:
<ul>
<li><code>my_pack_folder</code></li>
</ul>
</li>
</ul>
<hr>
<h2>Quick Example</h2>
<p>Put a datapack here:</p>
<ul>
<li><code>instance/lioloader/data/MyGlobalPack/pack.mcmeta</code></li>
<li><code>instance/lioloader/data/MyGlobalPack/data/&lt;namespace&gt;/...</code></li>
</ul>
<p>Put a resource pack here:</p>
<ul>
<li><code>instance/lioloader/resourcepacks/MyGlobalRP/pack.mcmeta</code></li>
<li><code>instance/lioloader/resourcepacks/MyGlobalRP/assets/&lt;namespace&gt;/...</code></li>
</ul>
<p>Then set priority:</p>
<ul>
<li><code>instance/lioloader/datapack_load_order.json</code></li>
<li><code>instance/lioloader/resourcepack_load_order.json</code></li>
</ul>
<h2><a href="https://discord.gg/sPHes7q4Pr" rel="nofollow"><img style="display: block; margin-left: auto; margin-right: auto;" src="https://i.ibb.co/qDNhg49/636e0a6a49cf127bf92de1e2-icon-clyde-blurple-RGB.png" width="128" height="97"></a></h2>
<p><img style="display: block; margin-left: auto; margin-right: auto;" src="https://i.ibb.co/hJvQHRN5/usageinmodpacks.png" alt="usageinmodpacks" width="602" height="41"></p>
<p style="text-align: center;">Feel free to include LioLoader in any pack of your choosing!</p>