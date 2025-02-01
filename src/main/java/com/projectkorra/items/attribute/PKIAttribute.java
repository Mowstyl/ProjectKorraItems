package com.projectkorra.items.attribute;

import java.util.ArrayList;
import java.util.List;

public class PKIAttribute {
	private final String name;
	private final String desc;
	private final List<Object> values;
	private final int benefit;
	private final boolean ability;

	/**
	 * Creates a new Attribute with a specific name, description, element, and
	 * benefit.
	 * 
	 * @param name the name of the Attribute
	 * @param desc a description of the Attribute
	 * @param benefit either -1 or 1, if increasing this attribute causes the
	 *            player to be better off then the benefit should be 1, else -1.
	 * @param ability if this attribute is related to an ability or a set of
	 *            abilities.
	 */
	public PKIAttribute(String name, String desc, int benefit, boolean ability) {
		this.name = name;
		this.desc = desc;
		this.benefit = benefit;
		this.ability = ability;
		values = new ArrayList<>();
	}

	/**
	 * Copies the details from another Attribute into this Attribute.
	 * 
	 * @param other the Attribute to copy
	 */
	public PKIAttribute(PKIAttribute other) {
		this.name = other.name;
		this.desc = other.desc;
		this.benefit = other.benefit;
		this.ability = other.ability;
		this.values = new ArrayList<>(other.values);
	}

	public PKIAttribute(String name, String desc, int benefit) {
		this(name, desc, benefit, false);
	}

	public PKIAttribute(String name, String desc, boolean ability) {
		this(name, desc, 1, ability);
	}

	public PKIAttribute(String name, String desc) {
		this(name, desc, 1, false);
	}

	public PKIAttribute(String name) {
		this(name, "");
	}

	public PKIAttribute() {
		this("");
	}

	public String getName() {
		return name;
	}

	public int getBenefit() {
		return benefit;
	}

	public boolean isAbility() {
		return ability;
	}

	public String getDesc() {
		return desc;
	}

	public List<Object> getValues() {
		return List.copyOf(values);
	}

	public void addValue(Object value) {
		values.add(value);
	}

	/**
	 * Gets an attribute from the list of attributes if the name matches an
	 * existing attribute.
	 * 
	 * @param name the name of the attribute
	 * @return an Attribute or null if none was found
	 */
	public static PKIAttribute getAttribute(String name) {
		if (name == null)
			return null;
		for (PKIAttribute att : AttributeList.ATTRIBUTES)
			if (att.getName().equalsIgnoreCase(name))
				return att;
		return null;
	}

	@Override
	public String toString() {
		return "Attribute [name=" + name + ", desc=" + desc + ", values=" + values + ", benefit=" + benefit + ", ability=" + ability + "]";
	}
}
