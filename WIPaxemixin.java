package mycf.timber.mixin;


import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(AxeItem.class)
public class axemixin extends MiningToolItem {
    protected axemixin(float attackDamage, float attackSpeed, ToolMaterial material, Set<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }
    Integer mode = 1;
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!world.isClient()){
            if(mode == 1){
                mode = 2;
                Text text = new LiteralText("Chop all");
                user.sendMessage(text,true);

            }else{
                mode = 1;
                Text text = new LiteralText("Chop 1");
                user.sendMessage(text,true);

            }
        }
        return super.use(world, user, hand);
    }

    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if(mode == 2) {
            if(isTwobyTwo(world,pos)){
                chopTwobyTwo(stack, world, state, pos, miner);
            } else {
                int i = 1;
                int ii = 1;
                for (int j = pos.getY(); j <= 255; j++) {
                    BlockState sta = world.getBlockState(pos.up(i));
                    BlockState stadown = world.getBlockState(pos.down(ii));
                    if (sta.isIn(BlockTags.LOGS)) {
                        i++;
                    } else if (stadown.isIn(BlockTags.LOGS)) {
                        ii++;
                    } else {
                        j = 266;
                    }
                }
                for (int k = 1; k <= i - 1; k++) {
                    world.breakBlock(pos.up(k), true);
                }
                for (int k = 1; k <= ii - 1; k++) {
                    world.breakBlock(pos.down(k), true);
                }
                int damages = ii + i - 1;
                if (!world.isClient && state.getHardness(world, pos) != 0.0F) {
                    stack.damage(damages, miner, (PlayerEntity) -> PlayerEntity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));

                }
            }
        }else{
            if (!world.isClient && state.getHardness(world, pos) != 0.0F) {
                stack.damage(1, miner, (PlayerEntity) -> PlayerEntity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            }
        }
        return true;
    }

    private boolean isTwobyTwo(World world, BlockPos pos){
        if(canTwobyTwo(world, pos)){
            if(world.getBlockState(pos.north()).isIn(BlockTags.LOGS))

            return (world.getBlockState(pos.north().east()).isIn(BlockTags.LOGS) && world.getBlockState(pos.east()).isIn(BlockTags.LOGS)) || (world.getBlockState(pos.north().west()).isIn(BlockTags.LOGS) && world.getBlockState(pos.west()).isIn(BlockTags.LOGS));
        } else if(world.getBlockState(pos.south()).isIn(BlockTags.LOGS)){

            return (world.getBlockState(pos.south().east()).isIn(BlockTags.LOGS) && world.getBlockState(pos.east()).isIn(BlockTags.LOGS)) || (world.getBlockState(pos.south().west()).isIn(BlockTags.LOGS) && world.getBlockState(pos.west()).isIn(BlockTags.LOGS));
        }
        return false;
    }

    private boolean isOAKorACACIA(BlockState state){
        return state.isIn(BlockTags.OAK_LOGS) || state.isIn(BlockTags.ACACIA_LOGS);
    }

    private boolean canTwobyTwo(World world, BlockPos pos){
        return world.getBlockState(pos).isIn(BlockTags.JUNGLE_LOGS) || world.getBlockState(pos).isIn(BlockTags.SPRUCE_LOGS) || world.getBlockState(pos).isIn(BlockTags.DARK_OAK_LOGS);
    }

    private void chopTwobyTwo(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        int countup = 0;
        int countdown = 0;
        for (int j = 0; j <= 260; j++) {
            if (canTwobyTwo(world, pos.up(countup))) {
                countup++;
            } else if (canTwobyTwo(world, pos.up(countdown))) {
                countdown++;
            } else {
                j = 300;
            }
        }
        for (int k = 1; k <= countup; k++) {
            if(stack.getDamage() != 0) {
                if (canTwobyTwo(world, pos.up(k).north())) {
                    if (canTwobyTwo(world, pos.up(k).east())) {
                        world.breakBlock(pos.up(k), true);
                        world.breakBlock(pos.up(k).north(), true);
                        world.breakBlock(pos.up(k).east(), true);
                        world.breakBlock(pos.up(k).north().east(), true);
                    } else if (canTwobyTwo(world, pos.up(k).west())) {
                        world.breakBlock(pos.up(k), true);
                        world.breakBlock(pos.up(k).north(), true);
                        world.breakBlock(pos.up(k).west(), true);
                        world.breakBlock(pos.up(k).north().west(), true);
                    }
                } else if (canTwobyTwo(world, pos.up(k).south())) {
                    if (canTwobyTwo(world, pos.up(k).east())) {
                        world.breakBlock(pos.up(k), true);
                        world.breakBlock(pos.up(k).south(), true);
                        world.breakBlock(pos.up(k).east(), true);
                        world.breakBlock(pos.up(k).south().east(), true);
                    } else if (canTwobyTwo(world, pos.up(k).west())) {
                        world.breakBlock(pos.up(k), true);
                        world.breakBlock(pos.up(k).south(), true);
                        world.breakBlock(pos.up(k).west(), true);
                        world.breakBlock(pos.up(k).south().west(), true);
                    }
                }
                if (!world.isClient) {
                    stack.damage(4, miner, (PlayerEntity) -> PlayerEntity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                }
            }
        }
        for (int k = 1; k <= countdown; k++) {
            if(stack.getDamage() != 0) {
                if (canTwobyTwo(world, pos.down(k).north())) {
                    if (canTwobyTwo(world, pos.down(k).east())) {
                        world.breakBlock(pos.down(k), true);
                        world.breakBlock(pos.down(k).north(), true);
                        world.breakBlock(pos.down(k).east(), true);
                        world.breakBlock(pos.down(k).north().east(), true);
                    } else if (canTwobyTwo(world, pos.up(k).west())) {
                        world.breakBlock(pos.down(k), true);
                        world.breakBlock(pos.down(k).north(), true);
                        world.breakBlock(pos.down(k).west(), true);
                        world.breakBlock(pos.down(k).north().west(), true);
                    }
                } else if (canTwobyTwo(world, pos.down(k).south())) {
                    if (canTwobyTwo(world, pos.down(k).east())) {
                        world.breakBlock(pos.down(k), true);
                        world.breakBlock(pos.down(k).south(), true);
                        world.breakBlock(pos.down(k).east(), true);
                        world.breakBlock(pos.down(k).south().east(), true);
                    } else if (canTwobyTwo(world, pos.down(k).west())) {
                        world.breakBlock(pos.down(k), true);
                        world.breakBlock(pos.down(k).south(), true);
                        world.breakBlock(pos.down(k).west(), true);
                        world.breakBlock(pos.down(k).south().west(), true);
                    }
                }
                if (!world.isClient) {
                    stack.damage(4, miner, (PlayerEntity) -> PlayerEntity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                }
            }
        }
    }
}
