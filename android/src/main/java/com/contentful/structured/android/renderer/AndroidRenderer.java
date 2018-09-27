package com.contentful.structured.android.renderer;

import com.contentful.structured.core.Checker;
import com.contentful.structured.core.Context;
import com.contentful.structured.core.Renderer;

/**
 * This is a combination interface for android renderer
 *
 * @param <C> context to be applied to this renderer.
 * @param <R> result of rendering a node.
 */
public interface AndroidRenderer<C extends Context, R> extends Checker<C>, Renderer<C, R> {
}
