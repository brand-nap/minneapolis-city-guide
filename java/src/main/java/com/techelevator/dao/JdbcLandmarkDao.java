package com.techelevator.dao;

import com.techelevator.model.Address;
import com.techelevator.model.Landmark;
import com.techelevator.model.Review;
import com.techelevator.model.Type;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcLandmarkDao implements LandmarkDao{

    private JdbcTemplate jdbcTemplate;
    private JdbcAddressDao jdbcAddressDao;

    public JdbcLandmarkDao(JdbcTemplate jdbcTemplate, JdbcAddressDao jdbcAddressDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcAddressDao = jdbcAddressDao;
    }

    @Override
    public List<Landmark> listLandmarks(){
        List<Landmark> list = new ArrayList<>();
        String sql = "SELECT landmarks.id, address_id, landmarks.name, landmarks.type, types.name AS type_name, description, likes, img_URL, is_pending" +
                " FROM landmarks " +
                " JOIN types ON landmarks.type = types.id " +
                " ORDER BY landmarks.name ";

        SqlRowSet result = jdbcTemplate.queryForRowSet(sql);
        while(result.next()){
            Landmark landmark = mapRowToLandmark(result);

            //add appropriate address to each landmark
            String addressSql = " SELECT id, street, city, state, zip " +
                " FROM addresses " +
                " WHERE id = ?";
            SqlRowSet addressResult = jdbcTemplate.queryForRowSet(addressSql, landmark.getAddressId());
            if (addressResult.next()){
                landmark.setAddress(mapRowToAddress(addressResult));
            }

            //add appropriate reviews to each landmark
            String reviewSql = " SELECT id, title, description, landmark_id, is_liked, user_id, username " +
                    " FROM reviews " +
                    " WHERE landmark_id = ?";
            SqlRowSet reviewResult = jdbcTemplate.queryForRowSet(reviewSql, landmark.getLandmarkId());
            while (reviewResult.next()){
                landmark.addReview(mapRowToReview(reviewResult));
            }

            list.add(landmark);
        }
        return list;
    }

    @Override
    public Landmark getLandmark(int landmarkId){
        List<Landmark> landmarks = listLandmarks();
        for(Landmark landmark: landmarks){
            if(landmark.getLandmarkId() == landmarkId){
                return landmark;
            }
        }
        return new Landmark();
    }


    //TODO: check sql
    @Override
    public boolean createLandmark(Landmark landmark, int addressId){

//        String addressSql = " INSERT INTO addresses (street, city, state, zip) " +
//                            " VALUES(?, ?, ?, ?) RETURNING id; ";
//
//        int addressId = jdbcTemplate.queryForObject(addressSql, int.class, landmark.getAddress().getStreet(), landmark.getAddress().getCity(), landmark.getAddress().getStateAbbrev(), landmark.getAddress().getZipCode());
//
//        //int addressId = jdbcAddressDao.listOfAddresses().size();

        String sql = "INSERT INTO landmarks (address_id, name, type, description, likes, img_URL, is_pending) " +
                    " VALUES(?, ?, ?, ?, ?, ?, ?); ";

         return jdbcTemplate.update(sql, addressId, landmark.getName(),
                                    landmark.getType().getTypeId(),
                                    landmark.getDescription(),
                                    landmark.getLikes(),
                                    landmark.getImgUrl(),
                                    landmark.isPending()) == 1;
    }

    @Override
    public Landmark updateLandmarkLikes(Landmark landmark, int landmarkId){
        Landmark result = landmark;
        int likeCount = landmark.getLikes() + 1;

        String sql = " UPDATE landmarks " +
                    " SET likes = ? " +
                    " WHERE id = ? ";
        int num = jdbcTemplate.update(sql, likeCount, landmarkId);

        if(num != 1){
            return null;
        }
        return result;
    }

    @Override
    public Landmark updatePendingStatus(Landmark landmark){
        Landmark result = landmark;

        String sql = " UPDATE landmarks " +
                " SET is_pending = ? " +
                " WHERE id = ? ";

        int num = jdbcTemplate.update(sql, false, landmark.getLandmarkId());

        if(num != 1){
            return null;
        }

        return result;
    }


    @Override
    public boolean deleteLandmark(int landmarkId){

        int lengthBefore = listLandmarks().size();

        String sql = " DELETE FROM landmarks " +
                " WHERE id = ? ";

        jdbcTemplate.update(sql, landmarkId);

        int lengthAfter = listLandmarks().size();

        if(lengthBefore == lengthAfter){
            return false;
        }
        return true;

    }




    private Landmark mapRowToLandmark(SqlRowSet results){
        Landmark landmark = new Landmark();

        landmark.setLandmarkId(results.getInt("id"));
        landmark.setAddressId(results.getInt("address_id"));
        landmark.setName(results.getString("name"));
        landmark.setType(mapRowToType(results));
        landmark.setDescription(results.getString("description"));
        landmark.setLikes(results.getInt("likes"));
        landmark.setImgUrl(results.getString("img_url"));
        landmark.setPending(results.getBoolean("is_pending"));
        return landmark;
    }
    private Address mapRowToAddress(SqlRowSet results){
        Address address = new Address();
        address.setAddressId(results.getInt("id"));
        address.setStreet(results.getString("street"));
        address.setCity(results.getString("city"));
        address.setStreet(results.getString("street"));
        address.setStateAbbrev(results.getString("state"));
        address.setZipCode(results.getInt("zip"));
        return address;
    }

    private Type mapRowToType(SqlRowSet results){
        Type type = new Type();
        type.setName(results.getString("type_name"));
        type.setTypeId(results.getInt("type"));
        return type;
    }

    private Review mapRowToReview(SqlRowSet results){
        Review review = new Review();
        review.setReviewId(results.getInt("id"));
        review.setTitle(results.getString("title"));
        review.setDescription(results.getString("description"));
        review.setLandmarkId(results.getInt("landmark_id"));
        review.setLiked(results.getBoolean("is_liked"));
        review.setUserId(results.getInt("user_id"));
        review.setUsername(results.getString("username"));

        return review;
    }
}
