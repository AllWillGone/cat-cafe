import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/login.vue'
import SignupOrdinaryUser from '../views/signupOrdinaryUser.vue'
import UserHome from '../views/UserHome.vue'
import UserIndex from '../views/UserIndex.vue'
import UserCats from '../views/UserCats.vue'
import UserProducts from '../views/UserProducts.vue'
import UserOrders from '../views/UserOrders.vue'
import UserLikes from '../views/UserLikes.vue'
import UserProfile from '../views/UserProfile.vue'
import CatDetail from '../views/CatDetail.vue'
import ProductDetail from '../views/ProductDetail.vue'
import AdminHome from '../views/adminHome.vue'
import AdminUsers from '../views/adminUsers.vue'
import AdminComments from '../views/adminComments.vue'
import AdminOrders from '../views/adminOrders.vue'
import AdminCats from '../views/adminCats.vue'
import AdminProducts from '../views/adminProducts.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/signup', component: SignupOrdinaryUser },
  {
    path: '/home',
    component: UserHome,
    redirect: '/home/index',
    children: [
      { path: 'index', component: UserIndex },
      { path: 'cats/:id', component: CatDetail },
      { path: 'cats', component: UserCats },
      { path: 'products/:id', component: ProductDetail },
      { path: 'products', component: UserProducts },
      { path: 'cart', component: () => import('../views/CartPage.vue') },
      { path: 'orders', component: UserOrders },
      { path: 'likes', component: UserLikes },
      { path: 'profile', component: UserProfile },
    ],
  },
  {
    path: '/admin',
    component: AdminHome,
    redirect: '/admin/users',
    children: [
      { path: 'users', component: AdminUsers },
      { path: 'comments', component: AdminComments },
      { path: 'orders', component: AdminOrders },
      { path: 'cats', component: AdminCats },
      { path: 'products', component: AdminProducts },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})
export default router
