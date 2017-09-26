package com.jombay.learnr.pojos;

/**
 * Created by Rishab on 26-09-2017.
 */

public class CurrentUserMeta
{
    private String _id;
    private String company_ids[];

    public String[] getCompany_ids()
    {
        return company_ids;
    }

    public String get_id()
    {
        return _id;
    }
}
