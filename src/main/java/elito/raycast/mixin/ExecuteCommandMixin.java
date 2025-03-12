package elito.raycast.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {

    @Inject(at = @At("HEAD"), method = "register")
    private static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, CallbackInfo ci){
        LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
                Commands.literal("execute")
                        .then(
                                Commands.literal("raycast")
                        )
        );

        commandDispatcher
                .register(
                    Commands.literal("execute")
                            .then(
                                    Commands.literal("raycast")
                                            .redirect(
                                                    literalCommandNode,
                                                    commandContext -> {
                                                        CommandSourceStack source = commandContext.getSource();

                                                        ServerLevel level = source.getLevel();
                                                        Vec3 origin = source.getPosition();

                                                        Vec2 rotation = source.getRotation();
                                                        Vec3 view = calculateViewVector(rotation.x,rotation.y);




                                                        if (isColliding(blockState, blockPos, position, level)) {

                                                        }

                                                        return source.withPosition(position);
                                                    }
                                            )
                            )
                );
    }

    @Unique
    private static Vec3 calculateViewVector(float f, float g) {
        float h = f * (float) (Math.PI / 180.0);
        float i = -g * (float) (Math.PI / 180.0);
        float j = Mth.cos(i);
        float k = Mth.sin(i);
        float l = Mth.cos(h);
        float m = Mth.sin(h);
        return new Vec3(k * l, -m, j * l);
    }

    @Unique
    private static boolean isColliding(BlockState blockState, BlockPos blockPos, Vec3 incomingPosition, ServerLevel world) {
        double d = 0.01;

        AABB box = AABB.ofSize(incomingPosition, d, d, d);

        return !blockState.isAir()
                && Shapes.joinIsNotEmpty(
                blockState.getCollisionShape(world, blockPos).move(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Shapes.create(box), BooleanOp.AND
        );
    }
}
