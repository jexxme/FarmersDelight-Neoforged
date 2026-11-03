package vectorwing.farmersdelight.data.tools;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/**
 * 26.3: vanilla {@link TagAppender} only accepts {@link ResourceKey}s. This adapter keeps the
 * object-based style of the original tag providers ({@code .add(ModItems.FOO.get())}).
 */
public final class FDTagAppender<T>
{
	private final TagAppender<T> delegate;
	private final Registry<T> registry;

	public FDTagAppender(TagAppender<T> delegate, Registry<T> registry) {
		this.delegate = delegate;
		this.registry = registry;
	}

	private ResourceKey<T> key(T element) {
		return registry.getResourceKey(element).orElseThrow(() -> new IllegalStateException("Unregistered tag element: " + element));
	}

	@SafeVarargs
	public final FDTagAppender<T> add(T... elements) {
		for (T element : elements) {
			delegate.add(key(element));
		}
		return this;
	}

	public FDTagAppender<T> add(ResourceKey<T> element) {
		delegate.add(element);
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
		delegate.remove(key(element));
		return this;
	}

	public FDTagAppender<T> replace() {
		delegate.replace();
		return this;
	}
}
