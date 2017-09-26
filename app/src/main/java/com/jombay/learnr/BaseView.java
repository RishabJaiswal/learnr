package com.jombay.learnr;

/**
 * Created by Rishab on 25-09-2017.
 * This interface provides the required abstraction
 * around Android Views from the presenter, since
 * the presenter should not know about the
 * Android Framework.
 */

public interface BaseView
{
    public void setPresenter();
}
