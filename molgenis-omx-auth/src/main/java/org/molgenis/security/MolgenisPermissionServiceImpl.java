package org.molgenis.security;

import static org.molgenis.security.SecurityUtils.GRANTED_AUTHORITY_SU;

import java.util.Collection;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class MolgenisPermissionServiceImpl implements MolgenisPermissionService
{
	@Override
	public boolean hasPermissionOnPlugin(String pluginId, Permission permission)
	{
		return hasPermission(pluginId, permission, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	public boolean hasPermissionOnEntity(String entityName, Permission permission)
	{
		return hasPermission(entityName, permission, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
	}

	private boolean hasPermission(String authorityId, Permission permission, String authorityPrefix)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) return false;

		String pluginAuthority = authorityPrefix + permission.toString() + '_' + authorityId.toUpperCase();
		Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
		if (grantedAuthorities != null)
		{
			for (GrantedAuthority grantedAuthority : grantedAuthorities)
			{
				String authority = grantedAuthority.getAuthority();
				if (authority.equals(GRANTED_AUTHORITY_SU.getAuthority()) || authority.equals(pluginAuthority)) return true;
			}
		}
		return false;
	}
}
