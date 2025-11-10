package net.redegs.digitizerplus.python.wrappers;

import net.minecraft.core.BlockPos;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.python.PythonRunner;
import net.redegs.digitizerplus.python.datatypes.PythonBlock;

import java.util.ArrayList;
import java.util.List;

public class PythonComputerWrapper {
    private ComputerEntity entity;

    public PythonComputerWrapper (ComputerEntity entity) {
        this.entity = entity;
    }

    public String getUUID() {
        return this.entity.getComputerID().toString();
    }

    public BlockPos getBlockPos() {
        return entity.getBlockPos();
    }






}
