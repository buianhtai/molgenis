package org.molgenis.migrate.version.v1_15;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;

import org.molgenis.data.IdGenerator;
import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step24UpdateApplicationSettings extends MolgenisUpgrade
{
	private final Logger LOG = LoggerFactory.getLogger(Step24UpdateApplicationSettings.class);

	private final JdbcTemplate jdbcTemplate;
	private final IdGenerator idGenerator;

	@Autowired
	public Step24UpdateApplicationSettings(DataSource dataSource, IdGenerator idGenerator)
	{
		super(23, 24);
		this.jdbcTemplate = new JdbcTemplate(requireNonNull(dataSource));
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating application settings ...");

		// add visible expression to sign up moderation setting
		String visibleExpression = "$('signup').eq(true).value()";
		String sql = "UPDATE attributes LEFT JOIN entities_attributes ON attributes.identifier = entities_attributes.attributes SET visibleExpression = ? WHERE fullName = ? and name= ?";
		jdbcTemplate.update(sql, visibleExpression, "settings_app", "signup_moderation");

		// add new settings between other settings
		for (int order = 4; order <= 12; ++order)
		{
			String orderSql = "UPDATE attributes LEFT JOIN entities_attributes ON attributes.identifier = entities_attributes.attributes SET `order` = ? WHERE fullName = ?";
			jdbcTemplate.update(orderSql, order + 2, "settings_app");
		}

		// add google_sign_in setting
		String googleSignInId = idGenerator.generateId();
		jdbcTemplate.update(
				"INSERT INTO attributes (`identifier`,`name`,`dataType`,`refEntity`,`expression`,`nillable`,`auto`,`idAttribute`,`lookupAttribute`,`visible`,`label`,`description`,`aggregateable`,`enumOptions`,`rangeMin`,`rangeMax`,`labelAttribute`,`readOnly`,`unique`,`visibleExpression`,`validationExpression`,`defaultValue`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				googleSignInId, "google_sign_in", "bool", null, null, false, false, false, false, true,
				"Enable Google Sign-In", "Enable users to sign in with their existing Google account", false, null,
				null, null, false, false, false, "$('signup').eq(true).value()", null, Boolean.TRUE.toString());

		jdbcTemplate.update("INSERT INTO entities_attributes (`order`, `fullName`, `attributes`) VALUES (?, ?, ?)", 4,
				"settings_app", googleSignInId);

		// add google_app_client_id setting
		String googleAppClientId = idGenerator.generateId();
		jdbcTemplate.update(
				"INSERT INTO attributes (`identifier`,`name`,`dataType`,`refEntity`,`expression`,`nillable`,`auto`,`idAttribute`,`lookupAttribute`,`visible`,`label`,`description`,`aggregateable`,`enumOptions`,`rangeMin`,`rangeMax`,`labelAttribute`,`readOnly`,`unique`,`visibleExpression`,`validationExpression`,`defaultValue`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				googleAppClientId, "google_app_client_id", "string", null, null, false, false, false, false, true,
				"Google app client ID", "Google app client ID used during Google Sign-In", false, null, null, null,
				false, false, false, "$('google_sign_in').eq(true).value()", null, Boolean.TRUE.toString());

		jdbcTemplate.update("INSERT INTO entities_attributes (`order`, `fullName`, `attributes`) VALUES (?, ?, ?)", 5,
				"settings_app", googleAppClientId);

		LOG.debug("Updated application settings");
	}
}
