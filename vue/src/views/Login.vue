<template>
  <div id="login" class="text-center">
     <link rel="stylesheet" href='https://fonts.googleapis.com/css?family=Montserrat Alternates'>
    <form id="form-signin" class="form-signin" @submit.prevent="login">
      
      <div
        class="alert alert-danger form-element"
        role="alert"
        v-if="invalidCredentials"
      >Invalid username and password!</div>
      <div
        class="alert alert-success form-element"
        role="alert"
        v-if="this.$route.query.registration"
      >Thank you for registering, please sign in.</div>
      <input
        type="text"
        id="username"
        class="form-control form-element"
        placeholder="Username or Email"
        v-model="user.username"
        required
        autofocus
      />
      <input
        type="password"
        id="password"
        class="form-control form-element"
        placeholder="Password"
        v-model="user.password"
        required
      />
      <button class="form-element login-button" type="submit">login</button>
      <button class="register-button"> <router-link class="form-element routerstyle" :to="{ name: 'register' }">join the club</router-link></button>
    </form>
    
    <h2 class="main-glider">Glider<img class="logo" src="../assets/Glider-1.png"></h2>
    

    
  
  </div>
 
</template>

<script>
import authService from "../services/AuthService";

export default {
  name: "login",
  components: {},
  data() {
    return {
      user: {
        username: "",
        password: ""
      },
      invalidCredentials: false
    };
  },
  methods: {
    login() {
      authService
        .login(this.user)
        .then(response => {
          if (response.status == 200) {
            this.$store.commit("SET_AUTH_TOKEN", response.data.token);
            this.$store.commit("SET_USER", response.data.user);

            //if user role is USER will route to home page
            if(this.$store.state.user.authorities[0].name === "ROLE_USER"){
                this.$router.push("/home");
            }
            //if user role is ADMIN will route to home page with pending landmark tab
            if (this.$store.state.user.authorities[0].name === "ROLE_ADMIN"){
              this.$router.push("/home/admin")
            } 
            else {
              this.$router.push("/home");
            }
            
          }
        })
        .catch(error => {
          const response = error.response;

          if (response.status === 401) {
            this.invalidCredentials = true;
          }
        });
    },
  }
};
</script>
<style>

body{
  background-color: #F3FCED;
}
div#login.text-center{
  height: 100vh;
  width: 100vw;
  margin: 0;
  display: grid;
  grid-template-areas: 
  "img login";
  grid-template-rows: 1fr 1fr;
}

img{
  grid-area: img;
  height: 10vh;
  justify-self: block-start;
}

#form-signin{
  grid-area: login;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-self: center;
  border: 5px #004E64 solid;
  border-radius: 10%;
  padding: 10%;
  background: rgb(246, 242, 242);
  width: 300px;
  height: 275px;
  
  margin-top:21vh;
}

.form-element {
  margin-top: 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 9px;
  
}

button.login-button{
  background-color: #004E64;
  border-radius: 12px;
  color: #F3FCED;
  font-family: 'Montserrat Alternates', 'Franklin Gothic Medium', 'Arial Narrow', 'Arial', 'sans-serif';
  font-size: 25px;
  margin-left: auto;
  margin-right: auto;
  width: 200px;
}

button.register-button{
  background-color: #1fd6c1;
  border-radius: 12px;
  color: #F3FCED;
  font-family: 'Montserrat Alternates', 'Franklin Gothic Medium', 'Arial Narrow', 'Arial', 'sans-serif';
  font-size: 25px;
  margin-left: auto;
  margin-right: auto;
  width: 200px;
}
.routerstyle{
  color: #F3FCED;
  text-decoration: none;
}
.main-glider{
  font-size: 20vh;
  display: flex;
  justify-content: center;
  margin-top:33vh;
}






</style>
