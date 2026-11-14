package com.travelsync.data;

import com.travelsync.model.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Produces realistic, relationally-consistent demo data so the application
 * never launches with an empty dashboard. Every booking references a real
 * traveler; every hotel/service/document references a real destination.
 */
public final class SampleDataGenerator {

    private SampleDataGenerator() { }

    public static List<Destination> generateDestinations() {
        List<Destination> list = new ArrayList<>();
        list.add(new Destination("D01", "Kyoto", "Japan", "Kansai",
                "Historic temples, gardens and geisha districts.", "Heritage", 92, 4.8, 5_200_000, 35.0116, 135.7681));
        list.add(new Destination("D02", "Santorini", "Greece", "Cyclades",
                "Whitewashed cliffside villages overlooking the Aegean Sea.", "Beach", 88, 4.7, 3_400_000, 36.3932, 25.4615));
        list.add(new Destination("D03", "Banff", "Canada", "Alberta",
                "Turquoise lakes and dramatic Rocky Mountain peaks.", "Mountain", 81, 4.9, 4_000_000, 51.1784, -115.5708));
        list.add(new Destination("D04", "Marrakech", "Morocco", "Marrakech-Safi",
                "Vibrant souks, palaces and desert gateway city.", "Urban", 76, 4.5, 2_500_000, 31.6295, -7.9811));
        list.add(new Destination("D05", "Queenstown", "New Zealand", "Otago",
                "Adventure capital surrounded by fjords and mountains.", "Adventure", 79, 4.8, 3_100_000, -45.0312, 168.6626));
        list.add(new Destination("D06", "Bali", "Indonesia", "Lesser Sunda Islands",
                "Tropical beaches, rice terraces and temple culture.", "Beach", 90, 4.6, 6_300_000, -8.3405, 115.0920));
        list.add(new Destination("D07", "Reykjavik", "Iceland", "Capital Region",
                "Gateway to glaciers, geysers and the northern lights.", "Nature", 74, 4.7, 2_100_000, 64.1466, -21.9426));
        list.add(new Destination("D08", "Cape Town", "South Africa", "Western Cape",
                "Table Mountain, coastline and winelands.", "Urban", 77, 4.6, 2_800_000, -33.9249, 18.4241));
        return list;
    }

    public static List<Attraction> generateAttractions() {
        List<Attraction> list = new ArrayList<>();
        list.add(new Attraction("A01", "Fushimi Inari Shrine", "D01", "Cultural",
                "Thousands of vermillion torii gates winding up the mountain.", 0, 4.8, 150));
        list.add(new Attraction("A02", "Arashiyama Bamboo Grove", "D01", "Nature",
                "A serene, towering bamboo forest walkway.", 0, 4.6, 60));
        list.add(new Attraction("A03", "Oia Sunset Point", "D02", "Landmark",
                "The iconic caldera-view sunset spot.", 0, 4.9, 90));
        list.add(new Attraction("A04", "Lake Louise", "D03", "Nature",
                "Glacier-fed turquoise lake ringed by peaks.", 10, 4.9, 120));
        list.add(new Attraction("A05", "Jardin Majorelle", "D04", "Cultural",
                "Cobalt-blue botanical garden once owned by Yves Saint Laurent.", 12, 4.5, 75));
        list.add(new Attraction("A06", "Milford Sound Cruise", "D05", "Adventure",
                "Fjord cruise beneath sheer cliffs and waterfalls.", 95, 4.9, 240));
        list.add(new Attraction("A07", "Tegallalang Rice Terraces", "D06", "Nature",
                "Iconic stepped rice paddies near Ubud.", 3, 4.4, 60));
        list.add(new Attraction("A08", "Blue Lagoon", "D07", "Nature",
                "Geothermal spa set in a lava field.", 65, 4.6, 150));
        list.add(new Attraction("A09", "Table Mountain Cableway", "D08", "Landmark",
                "Rotating cable car to panoramic summit views.", 30, 4.7, 120));
        return list;
    }

    public static List<Hotel> generateHotels() {
        List<Hotel> list = new ArrayList<>();
        list.add(new Hotel("H01", "Kyoto Gion Ryokan", "D01", 5, 320, 40, 6,
                new String[]{"Onsen", "Kaiseki Dining", "Garden View"}, 4.8));
        list.add(new Hotel("H02", "Santorini Caldera Suites", "D02", 5, 480, 30, 3,
                new String[]{"Infinity Pool", "Sea View", "Breakfast"}, 4.9));
        list.add(new Hotel("H03", "Banff Alpine Lodge", "D03", 4, 260, 60, 22,
                new String[]{"Spa", "Ski Storage", "Fireplace Lounge"}, 4.6));
        list.add(new Hotel("H04", "Marrakech Riad Azur", "D04", 4, 150, 25, 9,
                new String[]{"Rooftop Terrace", "Courtyard Pool"}, 4.5));
        list.add(new Hotel("H05", "Queenstown Lakeside Hotel", "D05", 4, 210, 80, 34,
                new String[]{"Lake View", "Gym", "Shuttle"}, 4.6));
        list.add(new Hotel("H06", "Bali Ubud Jungle Resort", "D06", 5, 190, 50, 14,
                new String[]{"Private Pool Villas", "Yoga Studio", "Spa"}, 4.7));
        list.add(new Hotel("H07", "Reykjavik Northern Lights Inn", "D07", 3, 140, 45, 20,
                new String[]{"Aurora Wake-up Call", "Sauna"}, 4.3));
        list.add(new Hotel("H08", "Cape Town Waterfront Hotel", "D08", 5, 275, 70, 18,
                new String[]{"Harbour View", "Rooftop Bar", "Pool"}, 4.6));
        return list;
    }

    public static List<Transportation> generateTransportation() {
        List<Transportation> list = new ArrayList<>();
        list.add(new Transportation("T01", "Flight", "D01", "D02", 180, 132, 620, 780));
        list.add(new Transportation("T02", "Flight", "D02", "D03", 220, 90, 890, 1150));
        list.add(new Transportation("T03", "Train", "D01", "D06", 400, 210, 340, 480));
        list.add(new Transportation("T04", "Flight", "D03", "D04", 160, 140, 1020, 960));
        list.add(new Transportation("T05", "Bus", "D04", "D07", 55, 40, 210, 540));
        list.add(new Transportation("T06", "Flight", "D05", "D06", 200, 60, 740, 690));
        list.add(new Transportation("T07", "Ferry", "D06", "D08", 120, 30, 980, 1320));
        list.add(new Transportation("T08", "Flight", "D07", "D08", 150, 95, 1150, 920));
        return list;
    }

    public static List<Traveler> generateTravelers() {
        List<Traveler> list = new ArrayList<>();
        String[] names = {"Amara Osei", "Liam Chen", "Sofia Ricci", "Noah Patel", "Elena Volkov",
                "Kenji Watanabe", "Fatima Zahra", "Owen Murphy", "Priya Nair", "Lucas Silva"};
        String[] nations = {"Ghana", "Singapore", "Italy", "India", "Russia", "Japan", "Morocco", "Ireland", "India", "Brazil"};
        String[] categories = {"Beach", "Heritage", "Mountain", "Adventure", "Urban"};
        Random rnd = new Random(42);
        for (int i = 0; i < names.length; i++) {
            list.add(new Traveler(String.format("TR%02d", i + 1), names[i], 
                    names[i].toLowerCase().replace(" ", ".") + "@travelmail.com",
                    nations[i], 100 + rnd.nextInt(4000), categories[rnd.nextInt(categories.length)]));
        }
        return list;
    }

    public static List<Booking> generateBookings(List<Traveler> travelers, List<Destination> destinations,
                                                   List<Hotel> hotels, List<Transportation> transportation) {
        List<Booking> list = new ArrayList<>();
        Booking.Status[] statuses = Booking.Status.values();
        Random rnd = new Random(7);
        LocalDate base = LocalDate.of(2026, 9, 1);
        for (int i = 0; i < 14; i++) {
            Traveler traveler = travelers.get(rnd.nextInt(travelers.size()));
            Destination destination = destinations.get(rnd.nextInt(destinations.size()));
            Hotel hotel = hotels.stream().filter(h -> h.getDestinationId().equals(destination.getId()))
                    .findFirst().orElse(hotels.get(0));
            Transportation transport = transportation.get(rnd.nextInt(transportation.size()));
            LocalDate start = base.plusDays(rnd.nextInt(90));
            LocalDate end = start.plusDays(3 + rnd.nextInt(7));
            double cost = hotel.getPricePerNight() * (end.getDayOfMonth() - start.getDayOfMonth() + 3) + transport.getPrice();
            Booking.Status status = statuses[rnd.nextInt(statuses.length)];
            list.add(new Booking(String.format("BK-%06X", rnd.nextInt(0xFFFFFF)),
                    traveler.getId(), destination.getId(), hotel.getId(), transport.getId(),
                    start, end, Math.round(cost * 100.0) / 100.0, status, start.minusDays(10 + rnd.nextInt(30))));
        }
        return list;
    }

    public static List<TravelService> generateServices() {
        List<TravelService> list = new ArrayList<>();
        list.add(new TravelService("S01", "Kyoto Tea Ceremony Tour", "Tour", "D01", 45, 15, 4.7));
        list.add(new TravelService("S02", "Santorini Sunset Sailing", "Tour", "D02", 120, 20, 4.9));
        list.add(new TravelService("S03", "Banff Airport Transfer", "Transfer", "D03", 60, 30, 4.4));
        list.add(new TravelService("S04", "Marrakech Hammam Spa", "Wellness", "D04", 55, 12, 4.6));
        list.add(new TravelService("S05", "Queenstown Bungee Adventure", "Adventure", "D05", 210, 25, 4.8));
        list.add(new TravelService("S06", "Bali Private Chef Dining", "Dining", "D06", 90, 8, 4.7));
        list.add(new TravelService("S07", "Reykjavik Glacier Hike", "Adventure", "D07", 175, 18, 4.8));
        list.add(new TravelService("S08", "Cape Town Wine Tour", "Tour", "D08", 85, 16, 4.6));
        return list;
    }

    public static List<TravelDocument> generateDocuments(List<Traveler> travelers, List<Booking> bookings) {
        List<TravelDocument> list = new ArrayList<>();
        String[] types = {"Visa", "Itinerary", "Insurance", "Ticket"};
        Random rnd = new Random(3);
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            String type = types[rnd.nextInt(types.length)];
            String content = "Travel document for booking " + b.getId() + " covering destination "
                    + b.getDestinationId() + ". Includes confirmed itinerary, transportation reference "
                    + b.getTransportationId() + " and accommodation reference " + b.getHotelId()
                    + ". Traveler must present this document along with a valid passport at checkpoints.";
            list.add(new TravelDocument("DOC" + (i + 1), type, b.getTravelerId(), b.getId(),
                    content, b.getCreatedOn(), b.getStartDate().plusYears(1), rnd.nextBoolean()));
        }
        return list;
    }
}
