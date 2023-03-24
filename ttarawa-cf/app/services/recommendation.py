import numpy as np
import pandas as pd
from flask import abort

from app.models.models import Tour


def haversine(lat1, lng1, lat2, lng2):
    # 지구 반경 (km)
    R = 6371.0088

    # 위도, 경도를 라디안 단위로 변환
    lat1, lng1, lat2, lng2 = map(np.radians, [lat1, lng1, lat2, lng2])

    # haversine 식 적용
    d_lat = lat2 - lat1
    d_lng = lng2 - lng1
    a = np.sin(d_lat / 2) ** 2 + np.cos(lat1) * np.cos(lat2) * np.sin(d_lng / 2) ** 2
    c = 2 * np.arctan2(np.sqrt(a), np.sqrt(1 - a))
    distance = R * c

    return distance


# def get_user_similar_destinations(user_info, data, num_destinations=30):
#     df = pd.read_csv(data, encoding='cp949')
#     vectors = df[['rating', 'reviews', 'search']].values
#     similarities = cosine_similarity([user_info], vectors)
#     similar_destinations_indices = similarities[0].argsort()[::-1][:num_destinations]
#     similar_destinations = df.loc[similar_destinations_indices]
#     return similar_destinations


def get_nearby_destinations(lat, lng, min_distance=0, max_distance=1, num_destinations=10):
    try:
        tours = Tour.query.with_entities(
            Tour.tour_id, Tour.address, Tour.category, Tour.lat, Tour.lng,
            Tour.mid_category, Tour.name, Tour.rating, Tour.reviews,
            Tour.search, Tour.sub_category
        ).all()

        df = pd.DataFrame(tours,
                          columns=['tour_id', 'address', 'category', 'lat', 'lng', 'mid_category', 'name', 'rating',
                                   'reviews', 'search', 'sub_category'])

        distances = df.apply(lambda row: haversine(lat, lng, row['lat'], row['lng']),
                             axis=1)
        nearby_destinations = df[(distances >= min_distance) & (distances <= max_distance) & (df['rating'] >= 3.3) & (
                df['reviews'] >= 20)]
        shopping_destinations = nearby_destinations[nearby_destinations['mid_category'] == '쇼핑'][:4]
        other_destinations = nearby_destinations[nearby_destinations['mid_category'] != '쇼핑']
        nearby_destinations = pd.concat([shopping_destinations, other_destinations])
        nearby_destinations = nearby_destinations.sort_values(by='search', ascending=False).iloc[:num_destinations]
        return nearby_destinations
    except Exception as e:
        print(f'Flask Server Error : {e}')
        abort(500, str(e))


def get_recommendations(lat, lng, min_distance=0, max_distance=1, num_destinations=10, user_info=None):
    if user_info is not None:
        print('ERROR - 미구현')
        # similar_destinations = get_user_similar_destinations(user_info, data, num_destinations)
        # return similar_destinations
        abort(500, str("Not implemented"))
        return "ERROR - 미구현"
    else:
        nearby_destinations = get_nearby_destinations(lat, lng, min_distance, max_distance, num_destinations)
        return nearby_destinations