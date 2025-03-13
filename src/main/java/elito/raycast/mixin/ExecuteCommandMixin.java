package elito.raycast.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {

    @Inject(at = @At("TAIL"), method = "register")
    private static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, CallbackInfo ci){

        commandDispatcher
                .register(
                        Commands.literal("execute")
                                .then(
                                        Commands.literal("raycast")
                                                .then(
                                                        Commands.argument("ratio", DoubleArgumentType.doubleArg(0, 1))
                                                                .redirect(
                                                                        commandDispatcher.getRoot().getChild("execute"),
                                                                        commandContext -> {
                                                                            //Context
                                                                            CommandSourceStack source = commandContext.getSource();
                                                                            //Get dimension
                                                                            ServerLevel level = source.getLevel();
                                                                            //Get origin
                                                                            Vec3 origin = source.getPosition();
                                                                            //Get rotation
                                                                            Vec2 rotation = source.getRotation();

                                                                            double limit = DoubleArgumentType.getDouble(commandContext, "ratio");

                                                                            Vec3 destination = extend(origin, rotation, 128);
                                                                            //Clip through blocks with collision shape
                                                                            ClipContext clipContext = new ClipContext(origin, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());

                                                                            Vec3 clipResult = level.clip(clipContext).getLocation();
                                                                            double distance = origin.distanceTo(clipResult);

                                                                            Vec3 result = extend(origin, rotation, distance * limit);

                                                                            //Return found position
                                                                            return source.withPosition(result).withRotation(rotation);
                                                                        }
                                                                )
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
    private static Vec3 extend(Vec3 position, Vec2 rotation, double scale ) {

        Vec3 view = calculateViewVector(rotation.x,rotation.y);

        return position.add(view.scale(scale));
    }
}
