package org.molgenis.data.support;

import java.util.*;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

public class DefaultEntityMetaData extends AbstractEntityMetaData
{
	private final String name;
	private final Map<String, AttributeMetaData> attributes = new LinkedHashMap<String, AttributeMetaData>();
	private String label;
    private boolean abstract_ = false;
	private String description;
	private String idAttribute;
	private String labelAttribute; //remove?
    private EntityMetaData extends_;

	public DefaultEntityMetaData(String name)
	{
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void addAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		if (attributeMetaData == null) throw new IllegalArgumentException("AttributeMetaData cannot be null");
		if (attributeMetaData.getName() == null) throw new IllegalArgumentException(
				"Name of the AttributeMetaData cannot be null");

		attributes.put(attributeMetaData.getName().toLowerCase(), attributeMetaData);
	}

	public void addAllAttributeMetaData(List<AttributeMetaData> attributeMetaDataList)
	{
		for (AttributeMetaData attributeMetaData : attributeMetaDataList)
		{
			if (attributeMetaData == null) throw new IllegalArgumentException("AttributeMetaData cannot be null");
			if (attributeMetaData.getName() == null) throw new IllegalArgumentException(
					"Name of the AttributeMetaData cannot be null");

			attributes.put(attributeMetaData.getName().toLowerCase(), attributeMetaData);
		}
	}

	@Override
	public List<AttributeMetaData> getAttributes()
	{
		return Collections.unmodifiableList(new ArrayList<AttributeMetaData>(attributes.values()));
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		// primary key is first attribute unless otherwise indicate
		if (idAttribute != null)
		{
			AttributeMetaData att = getAttribute(idAttribute);
			if (att == null) throw new RuntimeException("getIdAttribute() failed: '" + idAttribute + "' unknown");
			return att;
		}
		else for (AttributeMetaData att : getAttributes())
			return att;
		return null;
	}

	public DefaultEntityMetaData setIdAttribute(String name)
	{
		this.idAttribute = name;
		return this;
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		if (labelAttribute != null)
		{
			AttributeMetaData att = getAttribute(labelAttribute);
			if (att == null) throw new RuntimeException("getLabelAttribute() failed: '" + labelAttribute + "' unknown");
			return att;
		}
		else return getIdAttribute();
	}

	public DefaultEntityMetaData setLabelAttribute(String name)
	{
		this.labelAttribute = name;
        return this;
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		if (attributeName == null) throw new IllegalArgumentException("AttributeName is null");
		return attributes.get(attributeName.toLowerCase());
	}

	@Override
	public String getLabel()
	{
		return label != null ? label : name;
	}

	public DefaultEntityMetaData setLabel(String label)
	{
		this.label = label;
		return this;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public DefaultEntityMetaData setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DefaultEntityMetaData other = (DefaultEntityMetaData) obj;
		if (name == null)
		{
			if (other.name != null) return false;
		}
		else if (!name.equals(other.name)) return false;
		return true;
	}

	public DefaultAttributeMetaData addAttribute(String name)
	{
		DefaultAttributeMetaData result = new DefaultAttributeMetaData(name);
		this.addAttributeMetaData(result);
		return result;
	}

    @Override
    public boolean isAbstract() {
        return abstract_;
    }

    public void setAbstract(boolean abstract_) {
        this.abstract_ = abstract_;
    }

    @Override
    public EntityMetaData getExtends() {
        return extends_;
    }

    public void setExtends(EntityMetaData extends_) {
        this.extends_ = extends_;
    }
}
