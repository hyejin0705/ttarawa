import { StyleSheet, View, Text, Image, Dimensions } from 'react-native'
import { color } from '@styles/GlobalStyles'
import Label from '@components/common/Label'
import IconButton from '@components/common/IconButton'
import { MaterialCommunityIcons, Fontisto, Ionicons } from '@expo/vector-icons'

const SCREEN_HEIGHT = Dimensions.get('window').height

interface Props {
  historyId: number
  // User Info
  userName?: string
  userImg?: string
  rank?: string
  // rank?: (params: any) => any

  // 경로 이미지
  imagePath: string
  // imagePath: (params: any) => any

  // 공개 여부
  isLock?: number | boolean
  pressLock: (params: any) => any | undefined // 공개, 비공개 전환 함수

  // 좋아요
  likes: number // 좋아요 수
  isLike: number | boolean // 좋아요 누른 여부
  pressLike: (params: any) => any // 좋아요 전환 함수

  // 공유 함수
  pressShare?: (params: any) => any

  // 수정메뉴 함수
  pressMenu?: (params: any) => any

  distence: string // 주행거리  ex) 3.5 km
  time: string // 주행시간  ex) 30분

  // 내용
  content: string
}

export default function FeedCard({
  historyId,

  userName,
  userImg,
  rank,

  imagePath,

  isLock,
  pressLock,

  likes,
  isLike,
  pressLike,

  pressShare,
  pressMenu,

  distence,
  time,

  content,
}: Props) {
  // 내용 띄어쓰기를 위한
  const contentText = content?.replace(/(!!|\?|\.)/g, '$&\n')

  const distanceText = `주행 거리 : ${distence}`
  const timeText = `주행 시간 : ${time}`

  return (
    <View style={styles.cardContainer}>
      {/* ID, Profile */}
      {userName ? (
        <View style={styles.userInfo}>
          <View style={styles.imgContainer}>
            <Image source={{ uri: userImg }} style={styles.userImg} />
          </View>
          <Text style={styles.userName}>{userName}</Text>
          <Image source={{ uri: rank }} style={{ width: 30, height: 20 }} />
        </View>
      ) : null}

      {/* 경로 이미지 */}
      <View>
        <Image
          resizeMode="contain"
          source={{ uri: imagePath }}
          style={styles.cardImg}
        />
        {/* <Image source={imagePath} style={styles.cardImg} /> */}
        {userName ? null : (
          <View style={styles.lock}>
            <IconButton
              icon1={
                isLock ? (
                  <Fontisto name="locked" size={24} color="black" />
                ) : (
                  <Fontisto name="unlocked" size={24} color="black" />
                )
              }
              press={() => {
                pressLock(historyId)
              }}
            />
          </View>
        )}
      </View>

      <View style={styles.cardContent}>
        {/*  like */}
        <View style={styles.likeContainer}>
          <View style={styles.like}>
            <IconButton
              icon1={
                <Ionicons
                  name="heart-sharp"
                  size={35}
                  color={isLike ? 'crimson' : 'black'} // 좋아요 누르면, 색 변환
                />
              }
              press={() => {
                pressLike(historyId)
              }} // 누르면, true <-> false 함수
            />
            <Text style={styles.likeNum}>{likes}명</Text>
            <Text style={styles.contentText}>
              이 이 코스를 달리고 싶어합니다.
            </Text>
          </View>

          {/* userName 유무에 따라 공유 or 수정 아이콘 */}

          <View>
            {userName ? (
              <IconButton
                icon1={
                  <Ionicons
                    name="paper-plane-outline"
                    size={24}
                    color="black"
                  />
                }
                // press
              />
            ) : (
              <IconButton
                icon1={
                  <MaterialCommunityIcons
                    name="dots-vertical"
                    size={24}
                    color="black"
                  />
                }
                press={pressMenu}
              />
            )}
          </View>
        </View>

        {/* Label */}
        <View style={styles.label}>
          <Label text={distanceText} />
          <Label text={timeText} />
        </View>

        {/* 내용 */}
        <Text ellipsizeMode="tail" numberOfLines={5} style={styles.cardText}>
          {contentText}
        </Text>
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  cardContainer: {
    height: SCREEN_HEIGHT * 0.85,
  },

  userInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 7,
    marginHorizontal: 10,
    marginVertical: 10,
  },

  imgContainer: {
    borderRadius: 40,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: color.secondary,
  },

  userImg: {
    margin: 5,
    width: 40,
    height: 40,
    borderRadius: 40,
  },

  userName: {
    fontSize: 20,
    fontWeight: 'bold',
  },

  cardImg: {
    height: 200,
    LeftRadius: 10,
    borderTopRightRadius: 10,
  },

  lock: {
    position: 'absolute',
    top: 10,
    right: 10,
  },

  cardContent: {
    paddingHorizontal: 20,
  },

  likeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginVertical: 10,
  },

  like: {
    flexDirection: 'row',
    alignItems: 'center',
  },

  likeNum: {
    fontSize: 18,
    fontWeight: 'bold',
  },

  contentText: {
    fontSize: 15,
  },

  label: {
    flexDirection: 'row',
    alignContent: 'center',
    gap: 10,
    marginBottom: 10,
  },

  cardText: {
    fontSize: 16,
  },
})
