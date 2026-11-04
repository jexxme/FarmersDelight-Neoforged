package vectorwing.farmersdelight.data.tools;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/**
 * Thin wrapper around the intrinsic (object-based) {@link TagAppender} of 26.1, so the tag providers share
 * their code with the 26.2+ builds, where vanilla only accepts {@link ResourceKey}s.
 */
public final class FDTagAppender<T>
{
	private final TagAppender<T, T> delegate;
	private final Registry<T> registry;

	public FDTagAppender(TagAppender<T, T> delegate, Registry<T> registry) {
		this.delegate = delegate;
		this.registry = registry;
	}

	@SafeVarargs
	public final FDTagAppender<T> add(T... elements) {
		for (T element : elements) {
			delegate.add(element);
		}
		return this;
	}

	public FDTagAppender<T> add(ResourceKey<T> element) {
		delegate.add(registry.getValueOrThrow(element));
		return this;
	}

	public FDTagAppender<T> add(TagEntry entry) {
		delegate.add(entry);
		return this;
	}

	public FDTagAppender<T> addTag(TagKey<T> tag) {
		delegate.addTag(tag);
		return this;
	}

	@SafeVarargs
	public final FDTagAppender<T> addTags(TagKey<T>... tags) {
		delegate.addTags(tags);
		return this;
	}

	public FDTagAppender<T> addOptionalTag(TagKey<T> tag) {
		delegate.addOptionalTag(tag);
		return this;
	}

	public FDTagAppender<T> remove(T element) {
		delegate.remove(element);
		return this;
	}

	public FDTagAppender<T> replace() {
		delegate.replace();
		return this;
	}
}
