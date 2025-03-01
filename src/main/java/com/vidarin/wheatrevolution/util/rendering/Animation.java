package com.vidarin.wheatrevolution.util.rendering;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/* SOME OF THIS CODE IS TAKEN FROM VEIL: https://github.com/FoundryMC/Veil/tree/1.20 */
public class Animation {
    List<Keyframe> frames;
    boolean loop;

    private Animation(boolean loop, Keyframe... frames) {
        this.frames = List.of(frames);
        this.loop = loop;
        addFrames();
    }

    public static Animation create(boolean loop, Keyframe... frames) {
        return new Animation(loop, frames);
    }

    private void addFrames() {
        List<Keyframe> newFrames = new ArrayList<>();
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            Keyframe frame = frames.get(frameIndex);
            newFrames.add(frame);
            for (int durationIndex = 0; durationIndex < frame.duration; durationIndex++) {
                int interpolatedFrameIndex = frameIndex + 1 >= frames.size() ? loop ? 0 : frameIndex : frameIndex + 1;
                newFrames.add(frame.interpolate(frames.get(interpolatedFrameIndex), durationIndex / ((float) frame.duration), frame.easing));
            }
        }
        frames = newFrames;
    }

    public Keyframe frameAtProgress(float progress) {
        return frames.get((int) ((frames.size() * progress) % (frames.size())));
    }

    public static class Keyframe {
        private final int duration;
        private final Easings.Easing easing;
        private Vec3 position = Vec3.ZERO;
        private Vec3 rotation = Vec3.ZERO;
        private Vec3 scale = Vec3.ZERO;

        public Keyframe(int duration) {
            this.duration = duration;
            this.easing = Easings.Easing.linear;
        }

        public Keyframe(int duration, Easings.Easing easing) {
            this.duration = duration;
            this.easing = easing;
        }

        public int getDuration() {
            return duration;
        }

        public Vec3 getPosition() {
            return position;
        }

        public Vec3 getRotation() {
            return rotation;
        }

        public Vec3 getScale() {
            return scale;
        }

        public Keyframe copy() {
            return new Keyframe(duration).withPosition(position).withRotation(rotation).withScale(scale);
        }

        public Keyframe withPosition(double x, double y, double z) {
            this.position = new Vec3(x, y, z);
            return this;
        }

        public Keyframe withRotation(double x, double y, double z) {
            this.rotation = new Vec3(x, y, z);
            return this;
        }

        public Keyframe withScale(double x, double y, double z) {
            this.scale = new Vec3(x, y, z);
            return this;
        }

        public Keyframe withPosition(Vec3 position) {
            this.position = position;
            return this;
        }

        public Keyframe withRotation(Vec3 rotation) {
            this.rotation = rotation;
            return this;
        }

        public Keyframe withScale(Vec3 scale) {
            this.scale = scale;
            return this;
        }

        public Keyframe interpolate(Keyframe frame, float progress, Easings.Easing easing) {
            Vec3 position = this.position.add(frame.position.subtract(this.position).scale(easing.ease(progress)));
            Vec3 rotation = this.rotation.add(frame.rotation.subtract(this.rotation).scale(easing.ease(progress)));
            Vec3 scale = this.scale.add(frame.scale.subtract(this.scale).scale(easing.ease(progress)));
            return new Keyframe(frame.duration, easing)
                    .withPosition(position)
                    .withRotation(rotation)
                    .withScale(scale);
        }
    }
}
