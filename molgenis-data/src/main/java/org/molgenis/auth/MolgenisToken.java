package org.molgenis.auth;

import static org.molgenis.auth.MolgenisTokenMetaData.CREATIONDATE;
import static org.molgenis.auth.MolgenisTokenMetaData.DESCRIPTION;
import static org.molgenis.auth.MolgenisTokenMetaData.EXPIRATIONDATE;
import static org.molgenis.auth.MolgenisTokenMetaData.ID;
import static org.molgenis.auth.MolgenisTokenMetaData.MOLGENIS_TOKEN;
import static org.molgenis.auth.MolgenisTokenMetaData.MOLGENIS_USER;
import static org.molgenis.auth.MolgenisTokenMetaData.TOKEN;

import java.util.Date;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

public class MolgenisToken extends SystemEntity
{
	public MolgenisToken(Entity entity)
	{
		super(entity, MOLGENIS_TOKEN);
	}

	public MolgenisToken(MolgenisTokenMetaData molgenisTokenMetaData)
	{
		super(molgenisTokenMetaData);
	}

	public MolgenisToken(String id, MolgenisTokenMetaData molgenisTokenMetaData)
	{
		super(molgenisTokenMetaData);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public MolgenisUser getMolgenisUser()
	{
		return getEntity(MOLGENIS_USER, MolgenisUser.class);
	}

	public void setMolgenisUser(MolgenisUser molgenisUser)
	{
		set(MOLGENIS_USER, molgenisUser);
	}

	public String getToken()
	{
		return getString(TOKEN);
	}

	public void setToken(String token)
	{
		set(TOKEN, token);
	}

	public Date getExpirationDate()
	{
		return getUtilDate(EXPIRATIONDATE);
	}

	public void setExpirationDate(Date expirationDate)
	{
		set(EXPIRATIONDATE, expirationDate);
	}

	public Date getCreationDate()
	{
		return getUtilDate(CREATIONDATE);
	}

	public void setCreationDate(Date creationDate)
	{
		set(CREATIONDATE, creationDate);
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}
}
