package com.jombay.learnr.presenter;

import com.jombay.learnr.BaseView;

/**
 * Created by Rishab on 25-09-2017.
 * This is class is required to handle
 * basic casting for the required presenters
 * based on the reference of their views
 */

class BasePresenter<V extends BaseView>
{
    V view;

    BasePresenter(BaseView baseView)
    {
        try
        {
            view = (V) baseView;
        } catch (ClassCastException cce)
        {

        }
    }
}
