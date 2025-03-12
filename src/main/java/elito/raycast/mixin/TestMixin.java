package elito.raycast.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExecuteCommand.class)
public class TestMixin {

    @Inject(at = @At("HEAD"), method = "register")
    private static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, CallbackInfo ci) {
        LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
                Commands.literal("execute")
                        .then(
                                Commands.literal("raycast")
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            Entity entity = source.getEntity();
                                            if (entity == null) {
                                                return 0;
                                            }

                                            Vec3 startPos = entity.getEyePosition();
                                            Vec3 lookVec = entity.getLookAngle().scale(100); // Extend ray 100 blocks
                                            Vec3 endPos = startPos.add(lookVec);

                                            ServerLevel level = source.getLevel();
                                            BlockHitResult hitResult = level.clip(new net.minecraft.world.level.ClipContext(
                                                    startPos, endPos, net.minecraft.world.level.ClipContext.Block.OUTLINE,
                                                    net.minecraft.world.level.ClipContext.Fluid.NONE, entity
                                            ));

                                            if (hitResult.getType() == HitResult.Type.BLOCK) {
                                                BlockPos hitBlockPos = hitResult.getBlockPos();
                                                BlockState hitBlockState = level.getBlockState(hitBlockPos);

                                                if (!hitBlockState.isAir()) {
                                                    Vec3 newPosition = Vec3.atCenterOf(hitBlockPos);
                                                    source.getEntity().teleportTo(newPosition.x, newPosition.y, newPosition.z);
                                                }
                                            }
                                            return 1;
                                        })
                        )
        );
    }
}
