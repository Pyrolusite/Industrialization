package dmillerw.industrialization.block.tile;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

import java.util.List;

/**
 * Created by Dylan Miller on 1/20/14
 */
public class TileConveyor extends TileCore {

    public ForgeDirection orientation = ForgeDirection.UNKNOWN;

    public float progress = 0.0F;

    public int storedID = 0;
    public int storedMeta = 0;

    public NBTTagCompound tileData;

    public boolean redstoneState = false;

    public float getRenderOffsetX() {
        return xCoord + (progress * orientation.getOpposite().offsetX);
    }

    public float getRenderOffsetY() {
        return yCoord + (progress * orientation.getOpposite().offsetY);
    }

    public float getRenderOffsetZ() {
        return zCoord + (progress * orientation.getOpposite().offsetZ);
    }

    @Override
    public void updateEntity() {
        if (redstoneState) {
            return;
        }

        if (storedID > 0) {
            if (progress < 1.0F) {
                progress += 0.1F;
            } else if (progress > 1.0F) {
                progress = 1.0F;
            }
        }

        if (!worldObj.isRemote) {
            int mX = xCoord + orientation.offsetX;
            int mY = yCoord + 1;
            int mZ = zCoord + orientation.offsetZ;

            if (!worldObj.isAirBlock(mX, mY, mZ)) {
                return;
            }

            List<EntityLivingBase> intersectingEntities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(mX, yCoord, mZ, mX + 1, yCoord + 1, mZ + 1));
            if (intersectingEntities != null && intersectingEntities.size() > 0) {
                return;
            }

            TileEntity potentialConveyor = worldObj.getBlockTileEntity(mX, yCoord, mZ);
            if (potentialConveyor != null && potentialConveyor instanceof TileConveyor) {
                if (((TileConveyor)potentialConveyor).storedID > 0) {
                    return;
                }
            }

            if (storedID == 0) {
                Block block = Block.blocksList[worldObj.getBlockId(xCoord, yCoord + 1, zCoord)];
                int meta = worldObj.getBlockMetadata(xCoord, yCoord + 1, zCoord);

                if (block != null && !block.isAirBlock(worldObj, xCoord, yCoord + 1, zCoord)) {
                    storedID = block.blockID;
                    storedMeta = meta;
                    if (block.hasTileEntity(meta)) {
                        TileEntity tile = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);

                        if (tile != null) {
                            NBTTagCompound nbt = new NBTTagCompound();
                            tile.writeToNBT(nbt);
                            nbt.setInteger("x", mX);
                            nbt.setInteger("y", mY);
                            nbt.setInteger("z", mZ);
                            tileData = nbt;

                            worldObj.removeBlockTileEntity(xCoord, yCoord + 1, zCoord);
                        }
                    }
                    worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord);
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                }
            } else if (progress == 1.0F && storedID > 0) {
                worldObj.setBlock(mX, mY, mZ, storedID, storedMeta, 3);
                if (tileData != null) {
                    TileEntity tile = worldObj.getBlockTileEntity(mX, mY, mZ);

                    if (tile != null) {
                        tile.readFromNBT(tileData);
                    }
                }
                worldObj.setBlockMetadataWithNotify(mX, mY, mZ, storedMeta, 3);
                worldObj.markBlockForUpdate(mX, mY, mZ);

                storedID = 0;
                storedMeta = 0;
                tileData = null;
                progress = 0.0F;

                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        redstoneState = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt) {
        nbt.setByte("orientation", (byte) orientation.ordinal());
        nbt.setBoolean("redstoneState", redstoneState);
        nbt.setFloat("progress", progress);
        nbt.setInteger("storedID", storedID);
        nbt.setInteger("storedMeta", storedMeta);
        if (tileData != null) {
            nbt.setCompoundTag("tileData", tileData);
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt) {
        orientation = ForgeDirection.values()[nbt.getByte("orientation")];
        redstoneState = nbt.getBoolean("redstoneState");
        progress = nbt.getFloat("progress");
        storedID = nbt.getInteger("storedID");
        storedMeta = nbt.getInteger("storedMeta");
        if (nbt.hasKey("tileData")) {
            tileData = nbt.getCompoundTag("tileData");
        } else {
            tileData = null;
        }
    }

}
