package ladysnake.effective.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import ladysnake.effective.Effective;
import ladysnake.effective.EffectiveUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.*;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

public class ChorusPetalParticle extends SpriteBillboardParticle {
	private static final Random RANDOM = new Random();
	protected final float rotationFactor;
	private final float groundOffset;
	private boolean isInAir = true;

	public ChorusPetalParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
		super(world, x, y, z, velocityX, velocityY, velocityZ);

		this.scale *= 1f + RANDOM.nextFloat();
		this.maxAge = 300 + random.nextInt(60);
		this.collidesWithWorld = true;
		int variant = RANDOM.nextInt(3);
		this.setSprite(spriteProvider.getSprite(variant, 2));

		if (velocityY == 0f && velocityX == 0f && velocityZ == 0f) {
			this.colorAlpha = 0f;
		}

		this.velocityY = velocityY - 0.15D - random.nextFloat() / 10;
		this.velocityX = velocityX - 0.05D - random.nextFloat() / 10;
		this.velocityZ = velocityZ - 0.05D - random.nextFloat() / 10;

		this.rotationFactor = ((float) Math.random() - 0.5F) * 0.01F;
		this.angle = random.nextFloat() * 360f;

		this.groundOffset = RANDOM.nextFloat() / 100f + 0.001f;
	}

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		this.colorRed = Math.max(this.colorGreen, 0.3f);

		Vec3d vec3d = camera.getPos();
		float f = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
		float g = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
		float h = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
		Quaternionf quaternion2;

		float i = 0f;
		if (this.angle == 0.0F) {
			quaternion2 = camera.getRotation();
		} else {
			quaternion2 = new Quaternionf(camera.getRotation());
			i = MathHelper.lerp(tickDelta, this.prevAngle, this.angle);
			// other = Vec3f.POSITIVE_Z.getRadialQuaternion(i)
			float otherz = Math.sin(i / 2.0F);
			float otherw = Math.cos(i / 2.0F);
			Quaternionf other = new Quaternionf(0, 0, otherz, otherw);
			quaternion2 = EffectiveUtils.hamiltonProduct(quaternion2, other);
		}

		Vector3f vec3f = new Vector3f(-1.0F, -1.0F, 0.0F);
		vec3f.rotate(quaternion2);
		Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
		float j = this.getSize(tickDelta);

		if (isInAir) {
			for (int k = 0; k < 4; ++k) {
				Vector3f vec3f2 = vector3fs[k];
				vec3f2.rotate(quaternion2);
				vec3f2.mul(j);
				vec3f2.add(f, g, h);
			}
		} else {
			for (int k = 0; k < 4; ++k) {
				Vector3f vec3f2 = vector3fs[k];
				float x = 90f, y = 0f, z = this.angle;
				x *= (float) (Math.PI / 180.0);
				y *= (float) (Math.PI / 180.0);
				z *= (float) (Math.PI / 180.0);

				float quatf = Math.sin(0.5F * x);
				float quatg = Math.cos(0.5F * x);
				float quath = Math.sin(0.5F * y);
				float quati = Math.cos(0.5F * y);
				float quatj = Math.sin(0.5F * z);
				float quatk = Math.cos(0.5F * z);
				x = quatf * quati * quatk + quatg * quath * quatj;
				y = quatg * quath * quatk - quatf * quati * quatj;
				z = quatf * quath * quatk + quatg * quati * quatj;
				float w = quatg * quati * quatk - quatf * quath * quatj;
				vec3f2.rotate(new Quaternionf(x, y, z, w));
				vec3f2.mul(j);
				vec3f2.add(f, g + this.groundOffset, h);
			}
		}

		float minU = this.getMinU();
		float maxU = this.getMaxU();
		float minV = this.getMinV();
		float maxV = this.getMaxV();
		int l = 15728880;

		vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(maxU, maxV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
		vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(maxU, minV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
		vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(minU, minV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
		vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(minU, maxV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
	}

	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	public void tick() {
		if (this.age++ < this.maxAge) {
			this.colorAlpha = Math.min(1f, this.colorAlpha + 0.1f);
		}

		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;

		this.move(this.velocityX, this.velocityY, this.velocityZ);
		this.velocityX *= 0.99D;
		this.velocityY *= 0.99D;
		this.velocityZ *= 0.99D;

		this.colorRed /= 1.001;
		this.colorGreen /= 1.002;

		if (this.age >= this.maxAge) {
			this.colorAlpha = Math.max(0f, this.colorAlpha - 0.1f);

			if (this.colorAlpha <= 0f) {
				this.markDead();
			}
		}

		this.prevAngle = this.angle;
		if (this.onGround || this.world.getFluidState(new BlockPos((int) this.x, (int) this.y, (int) this.z)).isIn(FluidTags.WATER)) {
			if (this.isInAir) {
				if (this.world.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z)).getBlock() == Blocks.WATER) {
					for (int i = 0; i > -10; i--) {
						BlockPos pos = new BlockPos((int) this.x, (int) (Math.round(this.y) + i), (int) this.z);
						if (this.world.getBlockState(pos).getBlock() == Blocks.WATER && this.world.getBlockState(new BlockPos((int) this.x, (int) (Math.round(this.y) + i), (int) this.z)).getFluidState().isSource() && this.world.getBlockState(new BlockPos((int) this.x, (int) (Math.round(this.y) + i + 1), (int) this.z)).isAir()) {
							this.world.addParticle(Effective.RIPPLE, this.x, Math.round(this.y) + i + 0.9f, this.z, 0, 0, 0);
							break;
						}
					}
				}

				this.velocityX = 0;
				this.velocityY = 0;
				this.velocityZ = 0;
				this.isInAir = false;
			}
		}

		if (this.velocityY != 0) {
			this.angle += Math.PI * Math.sin(rotationFactor * this.age) / 2;
		}
	}


	public static class DefaultFactory implements ParticleFactory<DefaultParticleType> {
		private final SpriteProvider spriteProvider;

		public DefaultFactory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
			return new ChorusPetalParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
		}
	}

}
