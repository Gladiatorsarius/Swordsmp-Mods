Testing checklist — ItemDisplay pick & hurt behavior

1. Launch client + server (development environment).
2. Ensure `solid-displays` mod is loaded and `ItemDisplayHP` gamerule is enabled.
3. Client pick tests:
   - Aim at an ItemDisplay from ~3-5 blocks; crosshair should show an entity hit (outline/tooltip) as if aiming at a block.
   - Try from several angles (front/side/top); pick should occur when ray intersects the display's 1x1 hitbox.
   - If already targeting a block behind a display, check that the display takes precedence when closer.
4. Server hurt forwarding tests:
   - Attack an ItemDisplay with a weapon; verify server-side HP is decreased via `/displayhp get <entity>` or existing commands.
   - Confirm hit sound/particles occur where the display is located.
   - Verify that killing the display triggers the expected behavior (if implemented in `ItemDisplayHPManager`).
5. Edge cases:
   - Spectator/creative reach differences: ensure client pick works from expected reach distances.
   - Multiplayer: another player's attacks should also forward damage to `ItemDisplayHPManager` on server.

Notes:
- If pick behavior does not occur, enable debug logs and verify the mixins are applied (`solid-displays.client.mixins.json`).
- Adjust `reach` in `ItemDisplayClientPickMixin` if your server/client uses different reach distances.
