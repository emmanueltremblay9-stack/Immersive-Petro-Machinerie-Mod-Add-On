package com.emman.immersivepetromachinery.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class FillOnlyFluidHandler implements IFluidHandler {
    private final FluidTank tank;

    public FillOnlyFluidHandler(FluidTank tank) {
        this.tank = tank;
    }

    @Override
    public int getTanks() {
        return tank.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tankIndex) {
        if (tankIndex != 0) {
            return FluidStack.EMPTY;
        }

        return tank.getFluidInTank(tankIndex).copy();
    }

    @Override
    public int getTankCapacity(int tankIndex) {
        return tankIndex == 0 ? tank.getTankCapacity(tankIndex) : 0;
    }

    @Override
    public boolean isFluidValid(int tankIndex, FluidStack stack) {
        return tankIndex == 0 && tank.isFluidValid(tankIndex, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return tank.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
