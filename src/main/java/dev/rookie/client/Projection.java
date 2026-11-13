package dev.rookie.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

// dünya koordinatını GUI ölçeğinde ekran koordinatına çevirir, arkadaysa null
public final class Projection {
    private Projection() {}

    public static double[] toScreen(MinecraftClient mc, Vec3d world) {
        Camera cam = mc.gameRenderer.getCamera();
        Vec3d c = cam.getPos();
        double yaw = Math.toRadians(cam.getYaw());
        double pitch = Math.toRadians(cam.getPitch());

        double dx = world.x - c.x, dy = world.y - c.y, dz = world.z - c.z;
        double sY = Math.sin(yaw), cY = Math.cos(yaw);
        double sP = Math.sin(pitch), cP = Math.cos(pitch);

        double fx = -sY * cP, fy = -sP, fz = cY * cP;
        double rx = -cY, rz = -sY;
        double ux = -sY * sP, uy = cP, uz = cY * sP;

        double zc = dx * fx + dy * fy + dz * fz;
        if (zc < 0.1) return null;
        double xc = dx * rx + dz * rz;
        double yc = dx * ux + dy * uy + dz * uz;

        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        double fovY = Math.toRadians(mc.options.getFov().getValue());
        double focal = (h / 2.0) / Math.tan(fovY / 2.0);

        return new double[] { w / 2.0 + xc / zc * focal, h / 2.0 - yc / zc * focal };
    }
}
