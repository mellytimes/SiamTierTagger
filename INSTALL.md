# SiamTierTagger - Install Guide

## Requirements
- A Paper or Leaf server running Minecraft 1.21.4
- Java 21 installed
- A web host for the resource pack that is reachable from the public internet
  (your nginx works), giving a direct download link to the zip

## 1. Build the plugin
1. Unzip SiamTierTagger.zip.
2. Open the folder in IntelliJ (it will import the Gradle project), or use a terminal.
3. Build it: run `./gradlew build`
4. The finished jar is at: build/libs/SiamTierTagger-1.0.0.jar

## 2. Install the plugin
1. Copy SiamTierTagger-1.0.0.jar into your server's `plugins/` folder.
2. Start the server once, then stop it. This creates `plugins/SiamTierTagger/config.yml`.

## 3. Configure the plugin (optional)
Edit `plugins/SiamTierTagger/config.yml`:
- gamemode: leave blank ("") to show each player's best tier, or set one mode
  (CRYSTALS, UHC, DIAPOT, NETHPOT, SMP, SWORD, AXE, MACE, CART, DIASMP)
- refresh-minutes: how often to pull the leaderboard from the API (default 5)

## 4. Host the resource pack
1. Upload SiamTier-Pack.zip to your web host so it has a direct download URL,
   for example: https://yourdomain/SiamTier-Pack.zip
2. Test that URL in a browser from OUTSIDE your network (phone on mobile data).
   It must download the zip immediately, not show a page.

## 5. Point the server at the pack
In `server.properties` add or edit these lines:

    resource-pack=https://yourdomain/SiamTier-Pack.zip
    resource-pack-sha1=0f272e9804fed4856ce4358620f3844fd3f340f3
    require-resource-pack=true

(require-resource-pack=true makes sure every player loads the pack so the icons
always show. Set it to false if you want it optional.)

## 6. Start and verify
1. Start the server.
2. Join and accept the resource pack when prompted.
3. You should see the gamemode icon and tier in the nametag above each ranked
   player, and `/tier` should list a player's tiers in chat.

## Notes and troubleshooting
- If you edit any icon and re-zip the pack, the hash changes. Recompute it with
  `sha1sum SiamTier-Pack.zip` and update the resource-pack-sha1 line, or clients
  keep loading the old cached version.
- Empty boxes or no icons: the client cached an old pack. Quit all the way to the
  title screen and rejoin, or delete the `server-resource-packs` folder inside
  your `.minecraft` and rejoin to force a fresh download.
- The nametag icon renders at a fixed text size (a Minecraft limit on player
  nametags). The icon size in chat and `/tier` is controlled by the height values
  in assets/minecraft/font/default.json inside the pack.
- Only players listed on the SiamTier leaderboard get a tag; unranked players
  show no icon, which is normal.
