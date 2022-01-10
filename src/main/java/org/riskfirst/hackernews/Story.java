package org.riskfirst.hackernews;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Story {

	Long id;
	String by;
	String type;
	Integer score;
	Long time;
	List<Long> kids;
	String url;
	String title;
	Integer descendants;
	String text;

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getBy() {
		return by;
	}
	public void setBy(String by) {
		this.by = by;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getScore() {
		return score;
	}
	public void setScore(Integer score) {
		this.score = score;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public List<Long> getKids() {
		return kids;
	}
	public void setKids(List<Long> kids) {
		this.kids = kids;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getDescendants() {
		return descendants;
	}
	public void setDescendants(Integer descendants) {
		this.descendants = descendants;
	}
	
	
	
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Story other = (Story) obj;
		return Objects.equals(id, other.id);
	}
	
	@Override
	public String toString() {
		return "Story [id=" + id + ", by=" + by + ", type=" + type + ", score=" + score + "]";
	}
	

	
	
}
