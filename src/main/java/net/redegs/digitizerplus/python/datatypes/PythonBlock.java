package net.redegs.digitizerplus.python.datatypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PythonBlock {
    public String name;
    public String id;
    public String faceHit;
    public List<Float> pos;

    private BlockPos blockPos;
    private BlockEntity blockEntity;

    public PythonBlock(BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable BlockPos position, @Nullable HitResult hr) {
        this.name = blockState.getBlock().getName().getString();
        this.id = String.valueOf(BuiltInRegistries.ITEM.getKey(blockState.getBlock().asItem()));
        this.blockPos = position;
        this.blockEntity = blockEntity;

        this.pos = new ArrayList<>();
        pos.add((float) blockPos.getX()); pos.add((float) blockPos.getX()); pos.add((float) blockPos.getX());



        if (hr != null) {
            BlockHitResult blockHit = (BlockHitResult) hr;
            this.faceHit = blockHit.getDirection().getName();
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    public BlockPos getBlockPosition() {
        return this.blockPos;
    }

    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
