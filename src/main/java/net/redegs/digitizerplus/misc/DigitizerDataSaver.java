package net.redegs.digitizerplus.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
public class DigitizerDataSaver extends SavedData {
    // Map to store lists of DigitalStacks, keyed by section UUIDs
    private final Map<UUID, List<DigitalStack>> sectionMap = new HashMap<>();

    // Add a DigitalStack to a specific section
    public void addDigitalStack(UUID sectionId, DigitalStack digitalStack) {
        // Get the list for the section, or create a new one if it doesn't exist

        List<DigitalStack> stackList = sectionMap.computeIfAbsent(sectionId, k -> new ArrayList<>());
        stackList.add(digitalStack);
        setDirty(); // Mark the data as dirty to ensure it gets saved
    }

    // Get all DigitalStacks in a specific section
    public List<DigitalStack> getDigitalStacks(UUID sectionId) {
        return sectionMap.getOrDefault(sectionId, Collections.emptyList());
    }

    // Remove a specific DigitalStack from a section
    public boolean removeDigitalStack(UUID sectionId, DigitalStack digitalStack) {
        List<DigitalStack> stackList = sectionMap.get(sectionId);
        if (stackList != null) {
            boolean removed = stackList.remove(digitalStack);
            if (removed) {
                setDirty(); // Mark the data as dirty to ensure it gets saved
            }
            return removed;
        }
        return false;
    }

    // Clear all DigitalStacks in a specific section
    public void clearSection(UUID sectionId) {
        if (sectionMap.containsKey(sectionId)) {
            sectionMap.get(sectionId).clear();
            setDirty(); // Mark the data as dirty to ensure it gets saved
        }
    }

    // Clear all sections (wipe all data)
    public void clearAll() {
        sectionMap.clear();
        setDirty(); // Mark the data as dirty to ensure it gets saved
    }

    // Save the data to NBT
    @Override
    public CompoundTag save(CompoundTag nbt) {
        // Iterate over each section
        for (Map.Entry<UUID, List<DigitalStack>> entry : sectionMap.entrySet()) {
            UUID sectionId = entry.getKey();
            List<DigitalStack> stackList = entry.getValue();

            // Create a ListTag for the DigitalStacks in this section
            ListTag stackListTag = new ListTag();
            for (DigitalStack digitalStack : stackList) {
                stackListTag.add(digitalStack.toNBT());
            }

            // Save the section UUID and its associated stack list
            CompoundTag sectionTag = new CompoundTag();
            sectionTag.putUUID("sectionId", sectionId);
            sectionTag.put("stacks", stackListTag);
            nbt.put(sectionId.toString(), sectionTag);
        }
        return nbt;
    }

    // Load the data from NBT
    public static DigitizerDataSaver load(CompoundTag nbt) {
        DigitizerDataSaver data = new DigitizerDataSaver();
        // Iterate over all keys in the NBT (each key represents a section)
        for (String key : nbt.getAllKeys()) {
            CompoundTag sectionTag = nbt.getCompound(key);
            UUID sectionId = sectionTag.getUUID("sectionId");
            ListTag stackListTag = sectionTag.getList("stacks", Tag.TAG_COMPOUND);

            // Load the DigitalStacks for this section
            List<DigitalStack> stackList = new ArrayList<>();
            for (Tag tag : stackListTag) {
                CompoundTag stackTag = (CompoundTag) tag;
                DigitalStack digitalStack = DigitalStack.fromNBT(stackTag);
                stackList.add(digitalStack);
            }

            // Add the section to the map
            data.sectionMap.put(sectionId, stackList);
        }
        return data;
    }

    // Factory method to create or load the data
    public static DigitizerDataSaver get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(DigitizerDataSaver::load, DigitizerDataSaver::new, "digitizerplussavedata");
    }
}