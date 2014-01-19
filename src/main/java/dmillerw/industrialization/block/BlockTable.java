package dmillerw.industrialization.block;

import dmillerw.industrialization.block.tile.TileCore;
import dmillerw.industrialization.block.tile.TileTable;
import dmillerw.industrialization.client.render.block.RenderBlockTable;
import dmillerw.industrialization.client.render.block.SimpleBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

/**
 * Created by Dylan Miller on 1/1/14
 */
public class BlockTable extends BlockCore {

    public BlockTable(int id) {
        super(id, Material.rock);

        setHardness(2F);
        setResistance(2F);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fx, float fy, float fz) {
        if (!world.isRemote) {
            TileEntity tile = world.getBlockTileEntity(x, y, z);

            if (tile != null && tile instanceof TileCore) {
                if (((TileCore)tile).onBlockActivated(player)) {
                    world.markBlockForUpdate(x, y, z);
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Icon getIcon(int side, int meta) {
        return Block.stone.getIcon(0, 0);
    }

    @Override
    public Class<? extends SimpleBlockRenderer> getRenderer() {
        return RenderBlockTable.class;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public TileCore getTile(int meta) {
        return new TileTable();
    }

}
