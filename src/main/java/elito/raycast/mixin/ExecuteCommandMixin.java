package elito.raycast.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import elito.raycast.ray.Cast;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static net.minecraft.server.command.CommandManager.*;
import static elito.raycast.Raycast.LOGGER;


@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {

    @Inject(at = @At("TAIL"), method = "register")
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci){

        dispatcher
                .register(
                        literal("execute")
                                .then(
                                        literal("raycast_surfaced")
                                                .redirect(
                                                        dispatcher.getRoot().getChild("execute"),
                                                        context -> {
                                                            ServerCommandSource source = context.getSource();

                                                            ServerWorld world = source.getWorld();

                                                            Vec3d origin = source.getPosition();

                                                            Vec2f rotation = source.getRotation();

                                                            BlockHitResult hit = Cast.cast(origin, rotation, world);
                                                            Vec3d destination = hit.getPos();

                                                            Vec2f rotationUpdate = Cast.getRotation(hit.getSide());

                                                            return source.withPosition(destination).withRotation(new Vec2f(rotationUpdate.y, rotationUpdate.x));
                                                        }
                                                )
                                )
                );
    }
}
