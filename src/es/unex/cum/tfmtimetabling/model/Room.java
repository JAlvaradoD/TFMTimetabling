package es.unex.cum.tfmtimetabling.model;

/** Modelo Aula (ITC 2007: Room)
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class Room {
	String roomId;
	int capacity;
	
	public Room(String roomId, int capacity) {
		super();
		this.roomId = roomId; // ID
		this.capacity = capacity; // Capacidad
	}

	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	@Override
	public String toString() {
		return "Room [roomId=" + roomId + ", capacity=" + capacity + "]";
	}
	
}
