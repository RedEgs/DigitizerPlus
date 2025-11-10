package net.redegs.digitizerplus.python.datatypes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class PythonEntity {
    private String name;
    private int id;
    private String type;
    private Vec3 position;

    public PythonEntity(Entity entity) {
        this.name = entity.getName().getString();
        this.id = entity.getId();
        this.type = entity.getType().toShortString();
        this.position = entity.position();
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Vec3 getPosition() {return this.position; }
}
