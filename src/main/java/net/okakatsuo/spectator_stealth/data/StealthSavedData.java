package net.okakatsuo.spectator_stealth.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class StealthSavedData extends SavedData {

    private static final String DATA_NAME = "spectatorstealth_next_login";

    private final Map<UUID, String> scheduledPlayers = new LinkedHashMap<>();

    private StealthSavedData() {
    }

    public static StealthSavedData create() {
        return new StealthSavedData();
    }

    public static StealthSavedData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        StealthSavedData data = new StealthSavedData();

        ListTag list = tag.getList("Players", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);

            if (!entry.hasUUID("UUID")) {
                continue;
            }

            UUID uuid = entry.getUUID("UUID");
            String name = entry.getString("Name");

            data.scheduledPlayers.put(uuid, name);
        }

        return data;
    }

    public static StealthSavedData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(
                                StealthSavedData::create,
                                StealthSavedData::load
                        ),
                        DATA_NAME
                );
    }

    public boolean isScheduled(UUID uuid) {
        return scheduledPlayers.containsKey(uuid);
    }

    public boolean schedule(UUID uuid, String name) {
        if (scheduledPlayers.containsKey(uuid)) {
            return false;
        }

        scheduledPlayers.put(uuid, name);
        setDirty();

        return true;
    }

    public boolean cancel(UUID uuid) {
        if (scheduledPlayers.remove(uuid) == null) {
            return false;
        }

        setDirty();
        return true;
    }

    public boolean consume(UUID uuid) {
        return cancel(uuid);
    }

    public Map<UUID, String> getScheduledPlayers() {
        return Collections.unmodifiableMap(scheduledPlayers);
    }

    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, String> player :
                scheduledPlayers.entrySet()) {

            CompoundTag entry = new CompoundTag();

            entry.putUUID("UUID", player.getKey());
            entry.putString("Name", player.getValue());

            list.add(entry);
        }

        tag.put("Players", list);

        return tag;
    }
}